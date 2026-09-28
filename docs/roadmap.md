# Roadmap

The order the model layer is being rebuilt in. `DESIGN.md` describes the target
architecture; this says which parts of it exist yet and what's next. Status
lives here as a field on each entry rather than as a separate description of
what the code contains, which would duplicate the code and go stale the same
way.

Entries are added as they're planned and their status is flipped as they land.
Read `docs/decisions/` alongside this: a decision log entry explains why
something is shaped the way it is, this says whether it exists yet.

The rebuild happens on a branch, per
[0002](decisions/0002-replace-the-model-layer-in-place.md). `master` keeps the
working application until entry 3 lands, the first one at parity with it.

## 1. Minimal watchable loop

Status: not started.

A world with a four-direction grid that owns distance, and `Entity`, `Species`,
`Component` and a `Genome` that holds values without mutating them yet. The
day's six steps (decide, resolve, feed, metabolise, world processes, clean up)
run in order. Rabbit's brain picks a random `Move`, and the resolver walks it a
tile at a time. The day's order is a shuffle over id order, which is already
the rule for entities without a speed gene. The decide step builds a
`Perception` through the default circular sense, even though the random brain
ignores it.

The interface talks to the world only through the snapshot and a command queue
covering spawn, reset and draining while paused. A census replaces the
population counting that parses display strings. A headless runner advances a
seeded world and reports population. `DeterminismTest` carries over.

Regression rules that land here: resetting the statistics does not break the
population graph, and removing an entity validates before it mutates anything.

See [0004](decisions/0004-creatures-return-intents.md),
[0005](decisions/0005-a-day-is-one-method.md),
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0010](decisions/0010-runs-replay-exactly-from-a-seed.md),
[0011](decisions/0011-traversal-goes-through-queries.md),
[0012](decisions/0012-tests-target-brains-without-a-world.md),
[0013](decisions/0013-perception-is-one-type.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0015](decisions/0015-geometry-lives-in-the-grid.md).

## 2. Eating and starving

Status: not started.

Grass as an `Edible` entity, and a world process that spawns it. `Metabolism`
holds a reserve its carrier can see and its neighbours cannot. The feed step
takes every edible on the tile, and the metabolise step charges on an interval
and removes anything whose reserve falls below zero. Intervals and charges are
the first scalar `Setting`s, with bounds that reject non-positive values, which
is where that regression rule lands.

Rabbit gets the fixed-rules brain that approaches the nearest edible. Its rule
set is a design decision per
[0018](decisions/0018-brain-computation-model-and-warm-starting.md), so it gets
a log entry before it is built. That entry also settles whether walking away
from a crowd survives, which decides whether the inverted crowd comparison rule
lands or goes to `docs/ideas/`.

A creature standing on grass is now the normal case, so the grid needs at least
a draw order for two occupants.

Depends on entry 1. See
[0013](decisions/0013-perception-is-one-type.md),
[0016](decisions/0016-feeding-takes-the-tile.md),
[0017](decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 3. Breeding and switchover

Status: not started.

`Breed` as an intent, with interval, cost and endowment as settings. `GeneSpec`
with scalar and simplex mutation, and a baseline genome per species. The step
distribution, view range, speed and the fixed-rules brain's parameters become
genes. Speed arrives with its metabolism charge, since a free speed gene pins
to its bound. The runner reports gene means.

Regression rules that land here: the step variation is not integer divided, the
step distribution stays valid, and view range is inherited and varied.

Then what the switchover needs: the parameters panel generated from declared
settings, the remaining interface operations as commands, and the old model
deleted. The branch merges to `master`.

Depends on entry 2. See
[0004](decisions/0004-creatures-return-intents.md),
[0006](decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0017](decisions/0017-every-number-is-a-setting-or-a-gene.md).

## 4. Rabbit learns

Status: not started.

A gate rather than a feature: selection has to be shown working before a second
species muddies it. A world setting turns mutation off, and the runner compares
mutating runs against that control across several seeds.

The entry is done when mutating runs reliably outlast or outnumber the control,
the brain's genes drift the same way across seeds, and speed settles short of
its bound. Getting there is tuning the default settings and food pressure until
differences between rabbits matter. If behaviour stays dull, widen what a rabbit
perceives or can do before reaching for the network brain, per `DESIGN.md`.

Depends on entry 3. See
[0012](decisions/0012-tests-target-brains-without-a-world.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 5. Predator

Status: not started.

A second species whose brain hunts Rabbit. Violence arrives here as a new
intent. Rabbit's brain gains a flee response to a perceived threat component,
which it knows without knowing the predator species. This is the first test of
capabilities meeting without having been introduced.

The entry decides whether a kill leaves remains, since whatever kills spawns
them. `docs/ideas/corpses-and-scavenging.md` holds that question.

Depends on entry 4. See
[0004](decisions/0004-creatures-return-intents.md),
[0013](decisions/0013-perception-is-one-type.md).
