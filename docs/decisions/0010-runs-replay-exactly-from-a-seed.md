# 0010. Runs replay exactly from a seed

## Status

Accepted, 2026-09-03. Revised 2026-10-04.

## Context

Emergence and breakage look identical from outside. The only thing that
separates enjoying a surprise from fearing it is being able to watch it again,
so reproducibility is a property of the vision.

The world already owns one seeded generator that every behaviour draws from, and
`DeterminismTest` guards that a seeded run replays exactly. That has to survive
the rebuild.

Two sources of incidental ordering exist today. Action order comes from
insertion order in a list, which is stable but chosen by nobody. And
`GridTile.collectFood` enumerates a `Hashtable` to pick the food with the fewest
contenders, so when several foods tie, which one a creature collects falls out
of hash enumeration order. Which creature wins a contested food is already a
stated rule using the seeded generator, so that half is sound today.

The 2022 code builds its generator as `new Random(seed)`. Java's other way, the
platform default generator, could change in a later release and silently alter
every recorded run. Naming an algorithm whose output for a seed the Java
specification fixes rules that out, and `L64X128MixRandom` is faster and
statistically stronger than `Random`.

Two orders change from one process to the next. `Map.copyOf` randomises its
iteration order each time the JVM starts, which a draft of
[0022](0022-warnings-fail-the-build-and-nothing-is-null.md) relied on, and
[0003](0003-entities-carry-components.md) keys an entity's components by class,
whose hash differs between processes. Looping over either makes the
same seed print different output in two processes while every test inside one
process passes, which is where `DeterminismTest` runs. Checking a single
process was the alternative to checking two, and it cannot see this.

## Decision

The world owns one random generator, passed to everything that draws from it. No
class constructs its own. It is built by naming its algorithm,
`L64X128MixRandom`.

Entities are held sorted by id, and asking the grid what stands on a tile
returns occupants in ascending id order. Every system iterates in ascending id
order, except that resolve and feed walk the day's speed order from
[0014](0014-resolve-order-comes-from-a-speed-gene.md), which is itself built
from the id order and the world's generator.

Deciding finishes before anything is applied.

Nothing in the domain iterates a collection whose order is not defined. A map or
set that is looped over is sorted, or keeps insertion order with insertion in id
order, and a hash map or hash set is only looked up in. Replay is checked across
processes as well as within one: a test runs `run` twice with the same seed in
two separate JVMs and compares the output byte for byte.

## Consequences

The hash-order tiebreak in `collectFood` has nothing left to break. A creature
takes every edible on its tile, per [0016](0016-feeding-takes-the-tile.md), so
which one it takes first does not matter.

A run replays exactly from its seed and its command log, so a strange outcome
can be reproduced, and a headless run can be compared against itself across
changes.

Sorted iteration is a cost paid on every query. A tile's occupants come back
sorted, so the grid either keeps them sorted on insert or sorts on read, and any
index or spatial structure added later has to preserve id order in its results.

The guarantee forecloses running any step in parallel. Nothing needs that today,
and if a large world ever did, the guarantee is what would have to give.
