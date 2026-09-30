# 0007. State leaves as a whole-world snapshot, commands go in

## Status

Accepted, 2026-09-03. Revised 2026-09-30.

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

A boundary written down and not checked gets crossed by the first quick fix.
Three ways of checking it were considered. A separate Maven module for the core
is a true compile-time guarantee, and it restructures the build: a parent pom,
the shaded jar, the release workflow's jar path and the Spotless ratchet. A
second compiler run over only the core package was tried and fails at the job:
limited to `java.base` it rejected Swing, and it let the interface package and
JFreeChart through, because the rest of the build is already on its classpath.
Running `jdeps` over the compiled classes from a test was tried against the same
three planted references and caught each one by class.

A dependency injection container was considered for wiring the pieces together.
It buys configuration and scoping for a program with about a dozen
collaborators, at the cost of startup magic, and `DESIGN.md` rules out framework
generality this one simulation does not need.

## Decision

The core is the domain of a hexagonal architecture and lives in the package
`domain`. It depends on nothing but itself and `java.base`: no Swing, no AWT, no
JFreeChart and no interface package. No simulation rule lives outside it.
Commands are its inbound port, and the snapshot and the census are its outbound
ports. The Swing interface, the population chart and the headless runner are
adapters on the outside, and each one sends commands in and reads snapshots and
census rows out.

A test runs `jdeps` over the compiled classes and fails when a class in `domain`
depends on anything outside `domain` and `java.base`, naming the class.

Dependencies are injected by constructor, by hand. A class receives its
collaborators rather than building them, which is how the world's one generator
reaches everything that draws from it, per
[0010](0010-runs-replay-exactly-from-a-seed.md). `Main` is the composition root:
it builds the world and the adapters and connects them, and nothing else
constructs either. There is no container.

Each day the world produces an immutable snapshot of everything: the day
number, the grid dimensions, and for each tile its scalar fields and the
entities standing on it with their species, sprite key and the values worth
displaying. The viewer is a person rather than a creature, so the audience rules
in [0013](0013-perception-is-one-type.md) do not limit what the snapshot shows.

```java
public record WorldSnapshot(int day, int width, int height, List<TileView> tiles) {
    public record TileView(GridPosition at, Map<String, Double> fields, List<EntityView> entities) {}
    public record EntityView(
            int id, String species, String spriteKey, Map<String, Double> info) {}
}
```

Commands are the only way in. Spawning entities, resetting the world, editing a
species' settings, resetting the statistics and setting an entity alight are all
commands. The interface never calls a model method that is not one.

Commands queue, and the queue drains at the start of a day, before anything
decides. Each command is recorded with the day it applied on, so a run
reproduces from its seed plus its command log. A command given to a paused world
drains immediately and produces a new snapshot without advancing the day, so
clicking food onto the grid while paused shows the food.

The world advances on the Swing event dispatch thread. A snapshot out and a
queue in are exactly the handoff a worker thread needs, so moving the world off
the event thread stays available and contained, and nothing needs it at a few
hundred tiles.

## Consequences

A headless run behaves exactly like a watched one, so a scripted run is possible
and the food regeneration bug cannot recur.

A boundary violation fails the test run, not the compile, so it shows in
`./mvnw verify` and in CI rather than in the editor. The check enforces more than
the interface boundary, since the domain cannot reach for AWT's `Point` either.

`GridPosition` survives the rebuild per
[0002](0002-replace-the-model-layer-in-place.md) and moves into `domain`. The
current world constructs `DataAnalytics`, which the test flags when pointed at
the old model. In the rebuild `DataAnalytics` is an adapter reading the census.

Wiring by hand keeps every dependency visible in one constructor call, and
adding a collaborator means editing `Main`.

At a few hundred tiles a snapshot is a few thousand small records per day. The
cost is real if the world grows to tens of thousands of tiles, at which point
dirty regions become the answer. Adding a method that returns changes since a
given version is additive, and no existing caller breaks, because the interface
never reaches into the model directly.

Every command is a type someone has to write, where reaching into the model was
free. The current interface performs around twenty operations, so that is twenty
things to name before the switchover.

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
