# Roadmap

The order the project is being rebuilt in. `DESIGN.md` describes the target
architecture; this says which parts of it exist yet and what's next. Status
lives here as a field on each entry rather than as a separate description of
what the code contains, which would duplicate the code and go stale the same
way.

Entries are added as they're planned and their status is flipped as they land.
Read `docs/decisions/` alongside this: a decision log entry explains why
something is shaped the way it is, this says whether it exists yet.

The rebuild happens on the `rebuild` branch, per
[0002](decisions/0002-replace-the-model-layer-in-place.md). `master` keeps the
2022 application until entry 4 lands, the first one at parity with it.

Each entry lists the 2022 features it brings back and the regression rules it
carries, as acceptance lines. A 2022 feature left out on purpose is in
`docs/ideas/`.

## 1. Minimal loop

Status: not started.

The first commit deletes the 2022 code, its tests, and the Swing smoke-test
skill with its tool under `tools/gui-smoke-test/`. `AGENTS.md` follows the tree
from then on. Everything new lives under `io.github.eliasdecoppel.humansimulation`.

A world with a four-direction grid that owns distance, and `Entity`, `Species`,
`Component` and a `Genome` whose values are checked against `GeneSpec.Scalar`
and `GeneSpec.Simplex` declarations when it is built. Mutation waits for entry
4. The day's six steps (decide, resolve, feed, metabolise, world processes,
clean up) run in order, and `advance()` returns the day's snapshot and census
row together. Rabbit is the one species. Its brain picks uniformly among the
grid's directions plus `Idle`, and rolls a `Move`'s distance from its step
distribution. The resolver walks a move a tile at a time and stops at the edge.
The day's order is a shuffle over id order, which is already the rule for
entities without a speed gene. The decide step builds a `Perception` through
the default circular sense, even though the random brain ignores it.

The numbers this needs have a declared home from the start. `Setting` arrives
with its first shape, a bounded scalar with a default, for the world's grid
size, 20 by 20, and starting population, 10 rabbits. Rabbit's species carries a
baseline genome: view range 3, bounded 1 to 10, and a step distribution of 0.6,
0.3 and 0.1 over distances one to three, bounded to lengths one to three. Every
rabbit gets it unchanged, since there is no mutation yet.

Commands cover spawning by count at random tiles or at a position, and resetting
the world with a new seed. The world has `submit` and `applyPending`, and no
paused state. Its command log is kept in memory, and `DeterminismTest` replays a
seed plus a log into a second world and compares the snapshots. A census row per
day replaces the population counting that parses display strings. A `jdeps`
test fails when the domain depends on anything outside itself and `java.base`.

`run --seed --days` advances one world and prints its census as JSON Lines, per
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).

2022 features that come back here: random wandering with an idle chance of one
in five. The 2022 edge handling, which rerolled a move up to ten times and then
stood still, is replaced by walking until blocked.

Regression rules that land here: the census covers every recorded day, movement
never walks off the step distribution, and removing an entity validates before
it mutates anything.

On the screen: nothing in a browser yet. `run --seed 42 --days 100` prints one
line per day with the rabbit count, and the same seed prints the same lines.
This is the one entry watched as data rather than on the page, per
[0002](decisions/0002-replace-the-model-layer-in-place.md).

See [0002](decisions/0002-replace-the-model-layer-in-place.md),
[0003](decisions/0003-entities-carry-components.md),
[0004](decisions/0004-creatures-return-intents.md),
[0005](decisions/0005-a-day-is-one-method.md),
[0006](decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0010](decisions/0010-runs-replay-exactly-from-a-seed.md),
[0011](decisions/0011-traversal-goes-through-queries.md),
[0012](decisions/0012-tests-target-brains-without-a-world.md),
[0013](decisions/0013-perception-is-one-type.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0015](decisions/0015-geometry-lives-in-the-grid.md),
[0017](decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).

## 2. Watching in a browser

Status: not started.

A Javalin adapter serves the page and an HTTP API, per
[0020](decisions/0020-the-interface-is-a-web-page.md). Worlds are held by an
unguessable id under a small cap and closed by deleting them. Each world has one
executor thread that advances it at the playback rate. Snapshots and census rows
stream over Server-Sent Events, where a slow viewer skips stale snapshots and
never census rows. `serve` opens the browser, and `serve --no-browser` takes
`--port 0`, prints a ready line with the port, and exits when idle, per
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).

The frontend is React with TypeScript and Vite, built into the jar by
`frontend-maven-plugin`. A PixiJS canvas draws the grid with a rabbit sprite
from a CC0 asset pack, glides each rabbit between its tiles by diffing
snapshots by id, fades rabbits in and out, and shows a count on a tile holding
more than one. The controls are step, play and pause, and playback rate;
spawning rabbits by count and by clicking a tile; resetting the world, which
draws a new seed from the clock and shows it; and the population chart with a
statistics reset that clears the adapter's history. Vitest covers the
frontend's diffing, gliding and fading, and JUnit covers the API against a
running server.

2022 features that come back here: spawning by count and by click, stepping and
automatic play with a rate, resetting the world, and the population graph with
a statistics reset.

Regression rules that land here: resetting the statistics does not break the
population graph.

On the page: a plain ground of 20 by 20 tiles with ten rabbits that glide
between tiles when you press play, fade in when spawned, and show a count where
they share a tile, beside the population chart and the current seed.

Depends on entry 1. See
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0010](decisions/0010-runs-replay-exactly-from-a-seed.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0020](decisions/0020-the-interface-is-a-web-page.md),
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).

## 3. Eating and starving

Status: not started.

Grass as an `Edible` entity, and a world process that spawns it. `Metabolism`
holds a reserve its carrier can see and its neighbours cannot. The feed step
takes every edible on the tile, and the metabolise step charges on an interval
and removes anything whose reserve falls below zero. Intervals and charges join
the settings as species settings, and how much food arrives and how often join
as world settings, with bounds that reject non-positive values. That is where
the regression rule rejecting a zero eating or breeding interval lands.

Rabbit gets the fixed-rules brain that approaches the nearest edible. Its rule
set is a design decision per
[0018](decisions/0018-brain-computation-model-and-warm-starting.md), so it gets
a log entry before it is built. Its parameters sit in the baseline genome as
plain values until entry 4. The rule set's log entry also settles whether
walking away from a crowd survives, which decides whether the inverted crowd comparison rule lands
or goes to `docs/ideas/`.

A creature standing on grass is now the normal case, so the grid draws a
creature above the ground cover it stands on, and the count from entry 2
counts creatures rather than grass.

2022 features that come back here: food arriving on an interval in a set
amount, seven pieces every seven days by default; a reserve charged on an
interval, one every fifteen days by default, with death below zero; spawning
food by count and by clicking a tile; and a gene weighing wandering against
walking to food. Of the four 2022 food choices, the rule set keeps the closest
edible and a random visible one, and decides whether the ones farthest from
other creatures and nearest other food come back. It also decides the order in
which a move toward a target closes the two axes, which 2022 did x first. The
2022 split of a food between everyone who reached it is replaced by taking the
tile in speed order, per [0016](decisions/0016-feeding-takes-the-tile.md).

On the page: grass with its own sprite appears every few days, drawn under any
rabbit on its tile. Rabbits head for grass they can see instead of only
wandering, and a rabbit that starves fades out. The chart rises and falls with
the food. Food can be spawned by count and by click.

Depends on entry 2. See
[0009](decisions/0009-terrain-is-entities-plus-tile-fields.md),
[0013](decisions/0013-perception-is-one-type.md),
[0016](decisions/0016-feeding-takes-the-tile.md),
[0017](decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 4. Breeding and switchover

Status: not started.

`Breed` as an intent, with interval, cost and endowment as settings. `GeneSpec`
gains scalar and simplex mutation, which turns the baseline values from entries
1 and 3 into genes that vary and pass to a child: the step distribution, view
range and the fixed-rules brain's parameters, plus speed. Speed arrives with its
metabolism charge, since a free speed gene pins to its bound. `run` reports gene
means.

2022 features that come back here: breeding on an interval when the reserve
covers the cost, every fifteen days at a cost of three by default, with the
child on its parent's tile; and mutation of every inherited trait. A child
starts from an endowment rather than the 2022 empty reserve.

Regression rules that land here: the step variation is not integer divided, the
step distribution stays valid, view range is inherited and varied, and a view
range never drops below one.

Then what the switchover needs: the parameters panel, which 2022 also had,
generated from declared settings, with editing a setting as a command. The
branch merges to `master`.

On the page: a newborn appears on its parent's tile, the population grows and
shrinks on its own, and a settings panel changes any declared number while the
world runs.

Depends on entry 3. See
[0002](decisions/0002-replace-the-model-layer-in-place.md),
[0004](decisions/0004-creatures-return-intents.md),
[0006](decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0017](decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 5. Rabbit learns

Status: not started.

A gate rather than a feature: selection has to be shown working before a second
species muddies it. A world setting turns mutation off, and the runner compares
mutating runs against that control across several seeds.

The entry is done when mutating runs reliably outlast or outnumber the control,
the brain's genes drift the same way across seeds, and speed settles short of
its bound. Getting there is tuning the default settings and food pressure until
differences between rabbits matter. If behaviour stays dull, widen what a rabbit
perceives or can do before reaching for the network brain, per `DESIGN.md`.

On the page: nothing new is required. What changes is behaviour you can see,
such as rabbits heading for food more directly after a few hundred days than at
the start.

Depends on entry 4. See
[0012](decisions/0012-tests-target-brains-without-a-world.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 6. Predator

Status: not started.

A second species whose brain hunts Rabbit. Violence arrives here as a new
intent. Rabbit's brain gains a flee response to a perceived threat component,
which it knows without knowing the predator species. This is the first test of
capabilities meeting without having been introduced.

The entry decides whether a kill leaves remains, since whatever kills spawns
them. `docs/ideas/corpses-and-scavenging.md` holds that question.

A predator that eats grass is not a predator, so this is where feeding stops
taking every edible. `docs/ideas/diet.md` holds how a creature says what it
eats.

On the page: a wolf with its own sprite, rabbits running from a wolf they can
see, and a kill that removes a rabbit at once.

Depends on entry 5. See
[0004](decisions/0004-creatures-return-intents.md),
[0013](decisions/0013-perception-is-one-type.md).

## 7. Inspecting creatures

Status: not started.

Looking at one creature closely. Clicking a tile shows what stands on it.
Selecting a creature shows its age in days, its reserve and its genes, and
draws its view range on the grid. A list of every creature sorts by age or by
reserve. The selection clears when the selected id stops resolving, per
[0009](decisions/0009-terrain-is-entities-plus-tile-fields.md).

2022 features that come back here: a creature's age, the list of humans sorted
by days survived or by food, selecting a human to see its information and view
range, and clicking a tile to see its contents.

Depends on entry 4. See
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0013](decisions/0013-perception-is-one-type.md),
[0020](decisions/0020-the-interface-is-a-web-page.md).

## 8. Looking good

Status: not started.

The pass that serves the first goal in `DESIGN.md`. Art in one consistent style
replaces the placeholder asset pack for every species and the ground. The page
gets a designed layout and theme, light and dark. Creatures hop rather than
slide, and births, deaths and kills get small effects. An effect that a diff of
two snapshots cannot see, such as which creature killed which, needs events on
the day's report, per [0020](decisions/0020-the-interface-is-a-web-page.md).

The entry is done when someone who has never seen the project wants to keep
watching. That is judged by watching rather than by a test.

On the page: everything above.

Depends on entry 6, so every species that exists by then gets its art. See
[0020](decisions/0020-the-interface-is-a-web-page.md).

## 9. Hosting

Status: not started.

The app as a link anyone can open. Each visitor's worlds belong to their
session, abandoned worlds are cleaned up, and a cap on worlds across sessions
keeps a spike in traffic from exhausting the machine. A container image and a
deploy workflow put it on Fly.io.

Depends on entry 4, and comes after entry 8 so the first link strangers open
shows the finished look. See
[0020](decisions/0020-the-interface-is-a-web-page.md),
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).
