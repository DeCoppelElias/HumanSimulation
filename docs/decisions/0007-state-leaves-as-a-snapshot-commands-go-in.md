# 0007. State leaves as a whole-world snapshot, commands go in

## Status

Accepted, 2026-09-03. Revised 2026-10-30.

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

A world could be built with a starting population, given as counts or
positions per species, and a reset could restore it. That was the first version
of this entry, written against the 2022 reset, which left an empty world that
matched nothing construction produced. It makes the population a construction
input beside the seed, and it keeps the first creatures out of the command log,
so the log alone does not say what a run started with. Building every world
empty removes the mismatch the other way.

A spawn command could carry the `Species` value itself. That is type-safe inside
Java, and it makes every adapter look the species up before building the
command, the same lookup written in `run`, the web adapter and the tests. A name
keeps the command a plain value that reads the same in the log as on the wire.

An experiment sometimes needs a first generation that starts somewhere other
than the species' baseline, such as the `mutation-ramp` preset from roadmap
entry 7, which starts rabbits from a genome that barely aims for food. `run`
could build a species with another baseline instead. That leaves the command log silent about what the
run started with, so the same seed and log replay differently unless the same
flag is passed again. Overrides on the spawn put the starting genome in the log.

Roadmap entry 9 exports the genomes alive on a day, so genomes have to leave the
domain. A query on the world that returns them only when asked costs nothing on
the days nobody exports, and it adds a third outbound port beside the snapshot
and the census. Roadmap entry 13 shows a selected creature's genes on the page,
which reads snapshots, so the genes would reach the snapshot anyway and the
query would be a second way to read them.

Entry 13 shows a selected creature's age and reserve, and lists every creature
sorted by either. An endpoint returning one entity's values would send less,
and the sorted list needs the values of every creature anyway, so it would need
a second endpoint and a fetch per day racing the stream. Filling `info` by
reflecting over every numeric field of every own view would show a new
component with no work, and it leaks values that mean nothing to a person, such
as the day a creature last bred, and names its keys from code, so two
`daysLeft` fields collide. Writing age and reserve in the world's snapshot
method by hand means editing the world for every new value.

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
are public: what builds a world, such as its dimensions and its species, the
commands, the snapshot with the values inside it, the
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
entities standing on it with their species, sprite key, the values worth
displaying and their genes, and the current value of every setting, grouped by owner per
[0017](0017-every-number-is-a-setting-or-a-gene.md). The viewer is a person rather than a creature, so the audience rules
in [0013](0013-perception-is-one-type.md) do not limit what the snapshot shows.

```java
public record WorldSnapshot(long seed, int day, int width, int height, List<String> species,
        List<TileView> tiles, Map<String, Map<String, Double>> settings) {
    public record TileView(GridPosition at, Map<String, Double> fields, List<EntityView> entities) {}
    public record EntityView(int id, String species, String spriteKey, boolean creature,
            Map<String, Double> info, Map<String, Double> genes) {}
}
```

`genes` holds every gene of the entity's genome by name, and is empty for an
entity without genes, such as grass. It is kept apart from `info`, which holds
values worth showing a person, because an export reads it back as a spawn's
genes.

Every component declares the values it shows a person, by name, beside the
views in [0013](0013-perception-is-one-type.md), and `info` is those of all its
carrier's parts. `Component` gives no default. A brain shows nothing and every
food shows its amount, so those two contracts give one. Two parts of one
carrier showing the same name is a species bug, and the snapshot refuses it. `Lifespan` shows `age`, and `daysToLive`
where its species sets a lifespan. `Metabolism` shows `reserve` and
`daysToStarve`, and `Plant` and `Meat` show `amount`. The rest show nothing.

```java
// on Component
Map<String, Double> shown();
```

Commands are the only way in. Spawning entities, resetting the world, editing a
setting or restoring its default, and setting an entity alight are all
commands. The interface
never calls a model method that is not one. A reset carries the seed the new
world uses, so the log replays it.

A world is built from its width, its height, a seed and the species it knows,
and starts empty on day 0. Its starting population is spawn commands, which an
adapter sends when it creates the world and again after a reset, so the seed and
the command log describe a run from its first creature.

A spawn command names its species by name, which the world checks against the
species it was built with. Spawning a count places each creature on a tile drawn
uniformly from the whole grid with the world's generator, independently, so two
can land on one tile. Spawning at a position places one. Spawns can be sent on
any day, not only the first.

A spawn of a count may carry gene overrides, from gene name to value. Each
member starts from its species' baseline with those values in place, and
mutation applies after, as it does to the baseline. The species itself does not
change. A spawn at a position carries none until something needs it.

```java
record Spawn(String species, int count, Map<String, Double> genes) implements Command {}
```

A reset with a seed leaves the world as constructing it with that seed and
sending its edited settings would: empty, day 0, ids counted from the start
again, and a command log whose first entry is the reset, followed by a
`SetSetting` for each value that differs from its default, per
[0017](0017-every-number-is-a-setting-or-a-gene.md). The dimensions, settings
and species stay. Ids and day numbers therefore repeat
across a reset, so a viewer treats a changed seed or a day that goes backwards
as a new run rather than diffing across it. The reset is recorded in the new
log at day 0, the day the world it builds starts on.

A command is validated before it changes anything. One that names an id which
no longer resolves, a species the world does not know, a position off the grid,
a count below one, or a gene override its species does not declare or puts
outside its bounds, throws and leaves the world as
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

A census row holds the day and the population of every species the world knows,
keyed by species name in name order. A species that has died out stays in the
row at zero, so a chart line does not break at an extinction. The row also
holds the mean of each gene per species, over the members alive at the end of
the day, from their raw values. A species with no members or no genes is left
out of the means, since a zero would read as a real mean. And it holds the
day's deaths per species, counted by the cause that ended each one, per
[0024](0024-a-part-can-end-its-carrier.md). A species with no deaths that day
is left out, and a reader takes a missing cause as zero.

```java
public record CensusRow(int day, Map<String, Integer> population,
        Map<String, Map<String, Double>> geneMeans,
        Map<String, Map<String, Integer>> deaths) {}
```

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

A fresh world shows nothing until something spawns into it, so each adapter
carries its own starting population: `run` takes it as a flag with a default,
and the page sends it when it creates or resets a world. That number lives in
an adapter rather than as a declared setting, which is acceptable because it is
input to a run, not a rule the world reads.

Reproducing a run means keeping the command log, so a bug report is a seed and a
list of commands rather than a seed. The log is most of a save format, which is
not a goal and is a thing to resist growing.

A setting edited by command cannot change partway through a day, so a system
reading a setting live sees one value for the whole day.
