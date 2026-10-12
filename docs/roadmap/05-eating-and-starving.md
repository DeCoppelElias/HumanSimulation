# 5. Eating and starving

Status: not started.

Grass as a species whose members carry an `Edible`, and a world process that
drops it. The feed and metabolise steps and the list of world processes join the
day here. A world is built with its processes, `new World(width, height, seed,
species, processes)`, and the four-argument constructor means none, so a test
gets no food it did not ask for. `serve` and `run` pass the food drop.

`Setting` arrives with its first shape, `Setting.Scalar(key, description, min,
max, step, defaultValue)`. A step of 1 makes a setting whole, read as an `int`.
A declaration rejects a default outside its bounds or off its step, and a check
rejects a value the same way, which is where the regression rule rejecting a
zero eating interval lands. A species declares its settings, and a world process
declares the world settings it reads. A world setting names its subject, since
it has no species to be grouped under. Entry 5 reads defaults only, and editing
arrives in entry 6.

`Metabolism` holds a reserve counted in days, and the days since its last
charge. The metabolise step adds one to that count, charges `eatingCharge` once
it reaches `eatingInterval`, and removes anything whose reserve falls below zero.
Its own view shows the reserve and the days left, computed from the live charge
and interval, and others see nothing. The feed step walks the day's order, and a
creature takes every edible on its tile, crediting its reserve with each one's
amount. Removing an entity validates before it mutates, the regression rule
that lands here since starvation is the first thing that removes one.

The food drop adds grass on each day that completes a multiple of its interval,
each on a tile drawn uniformly from the whole grid, wherever it lands.

| Setting | On | Default | Bounds |
|---|---|---|---|
| `eatingInterval` | rabbit | 1 | 1 to 30 |
| `eatingCharge` | rabbit | 1 | 1 to 100 |
| `startingReserve` | rabbit | 15 | 0 to 100 |
| `stride` | rabbit | 3 | 1 to 5 |
| `nourishment` | grass | 15 | 1 to 100 |
| `grassPerDrop` | world | 7 | 1 to 100 |
| `grassDropInterval` | world | 7 | 1 to 30 |

All of these step by 1.

Rabbit gets the scoring brain of
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md), with its
genes as plain values in the baseline genome until entry 6. `RandomBrain`, the
`steps` gene and `GeneSpec.Simplex` are deleted, per
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md).

A creature is an entity with a brain. The snapshot's `EntityView` says whether
it is one, and the snapshot carries the names of the species the world knows.
The grid draws a tile's ground cover, the lowest-id entity that is not a
creature, under its lowest-id creature, and the count from entry 3 counts
creatures. The census still counts every species, so grass gets a line on the
chart. One species selector, rabbit by default, drives both spawning by count
and spawning by clicking a tile. Creating or resetting a world on the page also
spawns seven grass.

2022 features that come back here: food arriving on an interval in a set
amount, seven pieces every seven days by default; a reserve charged on an
interval with death below zero, which in 2022 was one every fifteen days with
food worth one and is now one a day with grass worth fifteen, the same economy
counted in days; spawning food by count and by clicking a tile. The 2022 gene
weighing wandering against walking to food, and the four ways of choosing which
food, are replaced by the scoring brain, which also settles the order a move
closes the two axes in, since it scores the landing. Of the four 2022 food
choices, the one farthest from other creatures returns as a negative `rabbitPull`, and
the regression rule against its inverted crowd comparison lands as a test that
a negative `rabbitPull` prefers the landing farther from other rabbits. The 2022 split of a food
between everyone who reached it is replaced by taking the tile in day order, per
[0016](../decisions/0016-feeding-takes-the-tile.md).

On the page: grass with its own sprite appears every seven days, drawn under any
rabbit on its tile. Rabbits head for grass they can see, more directly when
hungry, and a rabbit that starves fades out. The chart rises and falls with the
food. Grass can be spawned by count and by click.

Depends on entry 3. See
[0005](../decisions/0005-a-day-is-one-method.md),
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0009](../decisions/0009-terrain-is-entities-plus-tile-fields.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0016](../decisions/0016-feeding-takes-the-tile.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md).
