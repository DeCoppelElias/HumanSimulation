# 0007. State leaves as a whole-world snapshot, commands go in

## Status

Accepted, 2026-09-03. Revised 2026-10-04.

## Context

The interface currently reaches into the model for whatever it needs, and one
simulation rule has escaped upward as a result. Food regeneration lives in the
manager's automatic advance, whose only caller is the interface controller, so a
headless run of the world spawns no food at all. That bug is the argument for a
boundary on its own.

Three shapes were considered for how state crosses it. A snapshot lets a client
render from one message. Events make it replay history to know the current
state. Dirty regions make it ask.

Commands also need a moment to land.
[0010](0010-runs-replay-exactly-from-a-seed.md) promises that a run replays
exactly from a seed. A watched run takes clicks at arbitrary moments, so the
seed alone does not describe it, and a command applied between two systems
breaks the rule that every creature in a day sees the same world. Applying a
command the moment it arrives is cheaper and makes the interface feel instant.
It also means the run someone actually watched, which is the one they want to
reproduce, is the one that cannot be.

A paused world still has to show a command at once, and something has to know
it is paused. The world could hold a paused flag set by pause and resume
commands, which fills the command log with entries that change nothing in the
simulation and gives the domain a state only an interface uses. Applying a
command whenever no day is running needs no flag, and makes the outcome depend
on when a call happens, which turns into a race once the world has its own
thread.

Resetting the statistics was a command in the first version of this entry. It
changes nothing in the simulation either, for the same reason as pausing.

A boundary written down and not checked gets crossed by the first quick fix.
Three ways of checking it were considered. A separate Maven module for the core
is a true compile-time guarantee, and it restructures the build: a parent pom,
the shaded jar, the release workflow's jar path and the Spotless ratchet. A
second compiler run over only the core package was tried and fails at the job:
limited to `java.base` it rejected Swing, and it let the interface package and
JFreeChart through, because the rest of the build is already on its classpath.
Running `jdeps` over the compiled classes from a test was tried against the same
three planted references and caught each one by class.

A reset could leave an empty world, as the 2022 reset did. That is simpler to
describe, and the reset world then matches no world that construction produces,
so the first thing anyone does after it is spawn rabbits by hand.

A snapshot returned by applying pending commands while paused could carry the
number of the day those commands belong to, the next one. The counter on a
paused page would then jump forward on a click and stay put on a step.

Subpackages inside the domain, such as one for the grid and one for genomes,
were considered for its layout. Every type crossing between them has to be
public, which lets the adapters reach the world's internals as well. Splitting
later is kept in `docs/ideas/domain-subpackages.md`.

A dependency injection container was considered for wiring the pieces together.
It buys configuration and scoping for a program with about a dozen
collaborators, at the cost of startup magic, and `DESIGN.md` rules out framework
generality this one simulation does not need.

## Decision

The core is the domain of a hexagonal architecture and lives in the package
`io.github.decoppelelias.humansimulation.domain`. It depends on nothing but
itself and `java.base`: no web framework, no JSON library, no AWT and no adapter
package. No simulation rule lives outside it.

The domain is one flat package. The world and the values that cross the boundary
are public: what builds a world, such as its settings, its species and its
starting population, the commands, the snapshot with the values inside it, the
census row and the day's report. Everything else is package-private, so the
compiler holds the rule from [0003](0003-entities-carry-components.md) that
nothing inside the aggregate changes except through the world. The command line
adapter lives in `.cli` and the web adapter in `.web`, and `Main` sits in the
root package.

Commands are the domain's inbound port, and the snapshot and the census are its
outbound ports. The web adapter from [0020](0020-the-interface-is-a-web-page.md)
and the command line from [0021](0021-agents-drive-worlds-over-http-and-run.md)
are on the outside, and each one sends commands in and reads snapshots and
census rows out.

Advancing a day returns a report holding the day's snapshot and its census row,
and the caller hands each to whatever reads it. There are no listeners to
register.

A test runs `jdeps` over the compiled classes and fails when a class in `domain`
depends on anything outside `domain` and `java.base`, naming the class.

Dependencies are injected by constructor, by hand. A class receives its
collaborators rather than building them, which is how the world's one generator
reaches everything that draws from it, per
[0010](0010-runs-replay-exactly-from-a-seed.md). `Main` is the composition root:
it builds the world and the adapters and connects them, and nothing else
constructs either. There is no container.

Each day the world produces an immutable snapshot of everything: the seed, the
day number, the grid dimensions, and for each tile its scalar fields and the
entities standing on it with their species, sprite key and the values worth
displaying. The viewer is a person rather than a creature, so the audience rules
in [0013](0013-perception-is-one-type.md) do not limit what the snapshot shows.

```java
public record WorldSnapshot(long seed, int day, int width, int height, List<TileView> tiles) {
    public record TileView(GridPosition at, Map<String, Double> fields, List<EntityView> entities) {}
    public record EntityView(
            int id, String species, String spriteKey, Map<String, Double> info) {}
}
```

Commands are the only way in. Spawning entities, resetting the world, editing a
species' settings and setting an entity alight are all commands. The interface
never calls a model method that is not one. A reset carries the seed the new
world uses, so the log replays it.

A reset with a seed leaves the world exactly as constructing it with that seed
would: day 0, ids counted from the start again, the starting population it was
built with, and a command log whose first entry is the reset. The
dimensions, settings and species stay. Ids and day numbers therefore repeat
across a reset, so a viewer treats a changed seed or a day that goes backwards
as a new run rather than diffing across it. The reset is recorded in the new
log at day 0, the day the world it builds starts on.

A command is validated before it changes anything. One that names an id which
no longer resolves, or a position off the grid, throws and leaves the world as
it was. The exception is part of the domain's contract, and an adapter turns it
into an error for whoever sent the command.

Commands queue, and the queue drains at the start of a day, before anything
decides. Each command is recorded with the day counter's value when it drained,
which counts days completed, so a run reproduces from its seed plus its command
log. The log is kept in memory and `DeterminismTest` replays it. It is not
written to a file.

The world has no paused state. An adapter that is paused submits a command and
then asks the world to apply what is pending, which drains the queue and
returns a new snapshot without advancing the day, so clicking food onto the
grid while paused shows the food. That snapshot keeps the current day number,
since no day completed, and no census row comes with it. Draining then or at the
start of the next day leaves the world identical, and the counter has the same
value at both moments, so the log records the same day either way.

The census row leaves with each day, and its history belongs to the adapter
that reads it. Resetting the statistics clears that history and is not a
command. A world reset starts the history afresh too, since the day numbers
start again.

Each world is owned by one thread, per
[0020](0020-the-interface-is-a-web-page.md), and the queue is how commands cross
from other threads.

## Consequences

A headless run behaves exactly like a watched one, so a scripted run is possible
and the food regeneration bug cannot recur.

A boundary violation fails the test run, not the compile, so it shows in
`./mvnw verify` and in CI rather than in the editor. The check enforces more than
the interface boundary, since the domain cannot reach for AWT's `Point` either.

The 2022 world constructs its own chart, which the test flags when pointed at
the old model. In the rebuild the chart is in the browser and reads census rows.

Wiring by hand keeps every dependency visible in one constructor call, and
adding a collaborator means editing `Main`.

At a few hundred tiles a snapshot is a few thousand small records per day. The
cost is real if the world grows to tens of thousands of tiles, at which point
dirty regions become the answer. Adding a method that returns changes since a
given version is additive, and no existing caller breaks, because the interface
never reaches into the model directly.

Every command is a type someone has to write, where reaching into the model was
free. The 2022 interface performs around twenty operations, and each one the
rebuild keeps is a command to name.

Two of them do not survive translation. Asking whether an entity is a human
becomes asking whether it has a brain, and asking a human for its view range
becomes reading a gene.

While the world is running, a command is visibly applied on the next day. At a
few days a second that reads as immediate, and at one day every ten seconds it
does not.

Reproducing a run means keeping the command log, so a bug report is a seed and a
list of commands rather than a seed. The log is most of a save format, which is
not a goal and is a thing to resist growing.

A setting edited by command cannot change partway through a day, so a system
reading a setting live sees one value for the whole day.
