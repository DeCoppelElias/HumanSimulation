# 6. Breeding and settings

Status: done.

`Breed` joins `Intent`, and `Option` arrives as a sealed type mirroring it, with
`Move(direction, distance, dx, dy)`, `Idle` and `Breed`, per
[0004](../decisions/0004-creatures-return-intents.md). `Options` holds the
grid's directions and one ordered list of options: idling, then the moves, then
breeding when it is legal. `Options.Landing` is gone. `MoveRule` and `BreedRule`
each have an `options` and a `resolve` method that take what they read, such as
the creature, the grid, its settings or the day, and the world's `options` and
its resolve switch are the two places that list every action. Only legal options
are offered.

A `Fertility` component holds the day of its carrier's birth or last breed.
Breeding is legal once `breedInterval` days have passed since then, while the
reserve is at least `childReserve + breedingLoss + parentMargin`. The parent
pays `childReserve + breedingLoss`, and the child spawns on its tile with a
mutated genome and `childReserve`. The scoring brain scores breeding as idling
plus a `breedPull` gene, per
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md).

`GeneSpec` gains scalar mutation: a uniform offset within plus or minus the
mutation size, clamped to the bounds, drawn from the world's generator one gene
at a time in layout order, per
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md). A child
gets its parent's genome mutated, and a spawned member gets its species'
baseline mutated, per
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md), so
seeded tests that pin positions shift.

`speed` joins the rabbit's genes, and the day's order sorts by it descending
over the shuffle, per
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md). Each charge
on the eating interval becomes `eatingCharge + speed * speedCharge +
round(viewRange) * viewCharge`, and days left reads that total.

| Gene | Bounds | Baseline | Mutation size |
|---|---|---|---|
| `breedPull` | 0 to 5 | 1 | 0.2 |
| `speed` | 0 to 1 | 0.5 | 0.05 |

| Setting | On | Default | Bounds | Step |
|---|---|---|---|---|
| `breedInterval` | rabbit | 15 | 1 to 100 | 1 |
| `childReserve` | rabbit | 15 | 0 to 100 | 1 |
| `breedingLoss` | rabbit | 30 | 0 to 200 | 1 |
| `parentMargin` | rabbit | 5 | 0 to 100 | 1 |
| `speedCharge` | rabbit | 0.1 | 0 to 2 | 0.01 |
| `viewCharge` | rabbit | 0.03 | 0 to 1 | 0.01 |

The world owns a table of current setting values, filled from the declared
defaults, per [0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md).
Components and world processes read it live through a handle, which the `Spawn`
gives a component, so `Metabolism`, `Edible` and the food drop stop reading
defaults. Values are grouped by owner, `world` or a species name, and a world
refuses a species named `world` or two processes declaring one key.
`SetSetting(owner, key, value)` and `RestoreDefaults(owner, key)`, with owner
and key optional, are commands, logged and replayed like the rest and accepted
over HTTP. A reset keeps edited values and logs each one that differs from its
default as a `SetSetting` right after the reset. The snapshot carries the
current values by owner, and `GET /worlds/{id}/settings` returns the
declarations in panel order.

`CensusRow` gains `geneMeans`, from species to gene to the mean of its raw value
over the members alive at the end of the day, leaving out a species with no
members or no genes. `run` prints it on each line and the census endpoint
carries it.

2022 features that come back here: breeding on an interval when the reserve
covers the cost, every fifteen days by default, with the child on its parent's
tile; mutation of every inherited trait; and the parameters panel. A child
starts from `childReserve` rather than the 2022 empty reserve, the cost is
counted in days of reserve, and the window stays open once the interval has
passed rather than opening only on multiples of the creature's age.

Regression rules that land here: view range is inherited and varied, a view
range never drops below one, and a zero breeding interval is rejected. The two
2022 rules about the step distribution, that its variation is not integer
divided and that it stays valid, guard nothing since 0023 removed it.

On the page: a newborn appears on its parent's tile, and the population grows
and shrinks on its own. A settings panel shows a World group, then a group per
species, with each setting's description beside it, and changes any declared
number while the world runs. Each edited setting is marked and has a restore
button, and one restores the whole panel. An edit in one tab shows in every tab
on the same world.

Depends on entry 5. See
[0002](../decisions/0002-replace-the-model-layer-in-place.md),
[0003](../decisions/0003-entities-carry-components.md),
[0004](../decisions/0004-creatures-return-intents.md),
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0020](../decisions/0020-the-interface-is-a-web-page.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md),
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md).
