# Roadmap

The order the project is being rebuilt in. `DESIGN.md` describes the target
architecture; this says which parts of it exist yet and what's next. Status
lives here as a field on each entry rather than as a separate description of
what the code contains, which would duplicate the code and go stale the same
way.

Entries are added as they're planned and their status is flipped as they land.
Read `docs/decisions/` alongside this: a decision log entry explains why
something is shaped the way it is, this says whether it exists yet.

Each entry is built on its own branch and merged into `master` when it is
finished, per [0002](decisions/0002-replace-the-model-layer-in-place.md), so
`master` holds the last finished entry. The 2022 application stays on the
`v1.0-original-2022` tag. Its inspection features wait for entry 9.

Each entry lists the 2022 features it brings back and the regression rules it
carries, as acceptance lines. A 2022 feature left out on purpose is in
`docs/ideas/`.

## 1. Minimal loop

Status: done.

The first commit deletes the 2022 code, its tests, and the Swing smoke-test
skill with its tool under `tools/gui-smoke-test/`, and the pom loses JFreeChart,
the headless test property and its Swing description. The build moves to Java
25, in the pom and in both workflows' `java-version`, compiles with every
warning as an error, formats every file with no Spotless ratchet, and adds
AssertJ and picocli, per
[0022](decisions/0022-warnings-fail-the-build-and-nothing-is-null.md). The
README is replaced with a short one saying what the project is becoming, how to
build it and how to use `run`. `AGENTS.md` follows the tree from then on.
Everything new lives under `io.github.decoppelelias.humansimulation`, with the
domain as one flat package, `run` in `.cli` and `Main` at the root, per
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md).

A world with a four-direction grid that owns distance, and `Entity`, `Species`,
`Component` and a `Genome` whose values are checked against `GeneSpec.Scalar`
and `GeneSpec.Simplex` declarations when it is built. Mutation waits for entry
6. A day decides, orders, resolves and takes its census, in that order, and
`advance()` returns the day's snapshot and census row together. Feed, metabolise
and the world processes join the day in entry 5, where they first do something.
Rabbit is the one species. Its brain picks uniformly among the directions its
`Options` carry plus `Idle`, and rolls a `Move`'s distance from its step
distribution. The resolver walks a move a tile at a time and stops at the edge.
The day's order is a shuffle over id order, which is already the rule for
entities without a speed gene. The decide step hands each brain a `Perception`,
built through the default circular sense even though the random brain ignores
it, and an `Options` holding the grid's directions, per
[0004](decisions/0004-creatures-return-intents.md).

A world is built from a width, a height, a seed and its species, and starts
empty. Rabbit's species carries a baseline genome: view range 3, bounded 1 to 10
with a mutation size of 1, and a step distribution of 0.6, 0.3 and 0.1 over
distances one to three, bounded to lengths one to three with a mutation size of
0.2. The mutation sizes are the 2022 ones, and nothing reads them until entry 6.
Every rabbit gets the baseline unchanged, since there is no mutation yet.
`Setting` waits for entry 5, the first time a rule reads one, so a species
carries no settings until then.

Commands cover spawning by count at random tiles or at a position, naming the
species, and resetting the world with a new seed. A count places each rabbit on
a tile drawn independently from the whole grid. A reset leaves the world exactly
as constructing it with that seed would: empty, on day 0. The day counter counts
days completed, so a new world is on day 0, and a snapshot from `applyPending`
keeps the current day. The world has `submit` and `applyPending`, and no paused
state. Its command log is kept in memory, and `DeterminismTest` replays a seed
plus a log into a second world and compares the snapshots. A census row per day
replaces the population counting that parses display strings. A `jdeps` test
fails when the domain depends on anything outside itself and `java.base`.

`run --days` advances one world and prints its census as JSON Lines, one line
per day from day 1, such as `{"seed":42,"day":1,"population":{"rabbit":10}}`.
`--seed` defaults to the clock, `--width` and `--height` to 20, and `--spawn
rabbit=10` gives the starting population, which a given `--spawn` replaces, per
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).

2022 features that come back here: random wandering with an idle chance of one
in five. The 2022 edge handling, which rerolled a move up to ten times and then
stood still, is replaced by walking until blocked.

A second determinism test runs `run` twice with one seed in two separate JVMs
and compares the output byte for byte, since an iteration order that changes per
process passes every test inside one. The census in this entry is a rabbit count
that never changes, so the test also compares every day's full snapshot printed
by a test-only probe, per
[0010](decisions/0010-runs-replay-exactly-from-a-seed.md).

Regression rules that land here: the census covers every recorded day, movement
never walks off the step distribution, and every command validates before it
mutates anything.

On the screen: nothing in a browser yet. `run --seed 42 --days 100` prints one
line per day with the rabbit count, and the same seed prints the same lines,
in two separate processes as well as within one.
Entries 1 and 2 are watched as data rather than on the page, per
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
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md),
[0022](decisions/0022-warnings-fail-the-build-and-nothing-is-null.md).

## 2. Serving worlds over HTTP

Status: done.

A Javalin adapter serves an HTTP API, per
[0020](decisions/0020-the-interface-is-a-web-page.md), listening on `127.0.0.1`
unless `--host` says otherwise. Worlds are held by an unguessable id, listed,
capped at 32 by `--max-worlds`, and closed by deleting them.

| Request | Does |
|---|---|
| `POST /worlds` | Creates an empty, paused world, 20 by 20 with a seed from the clock unless `width`, `height` or `seed` say otherwise, at most 25 a side. |
| `GET /worlds` | Lists each world's id, size, seed, day, population and whether it plays. |
| `GET /worlds/{id}`, `DELETE /worlds/{id}` | The current snapshot, or closing the world. |
| `POST /worlds/{id}/commands` | One command, such as `{"type":"spawn","species":"rabbit","count":3}`. On a paused world it applies at once and returns the snapshot, on a playing one it returns `202` and lands the next day. A `reset` without a seed draws one from the clock. |
| `POST /worlds/{id}/step` | Advances a paused world `days` days, 1 by default and at most 10,000, and returns the last day's report. One step at a time per world. |
| `POST /worlds/{id}/play`, `/pause`, `PUT /worlds/{id}/rate` | The world's timer, at 0.5 to 10 days a second, 2 by default. |
| `GET /worlds/{id}/census?from=N`, `DELETE /worlds/{id}/census` | The census history, or the statistics reset. |
| `GET /worlds/{id}/log` | Size, seed and commands, in the shape the commands route takes. |
| `GET /worlds/{id}/events` | Server-Sent Events. |

Errors are `{"error":"..."}`: `400` for input the domain or the limits reject,
`404` for an unknown world, `409` for stepping a playing world, a second step
in flight, or creating past the cap.

Each world has one executor thread that owns it and advances it at the playback
rate. The stream is fire and forget: each viewer holds the latest day's
snapshot with its census row, a slow viewer skips days, and a reader that sees
a gap fetches the missing rows from the history. A statistics reset sends
`censusReset`, and a world reset clears the history too, since its day numbers
start again. A `: keepalive` comment goes out every 15 seconds.

`serve` prints `{"event":"ready","url":...,"port":...}` on stdout once it
accepts requests, takes `--port 0`, and logs to stderr, per
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md). `serve
--no-browser` quits after 10 minutes without a request or a stream. `serve`
never quits, does not open a browser until entry 3, and pauses playing worlds
after 24 hours without a request or a stream. `--idle-minutes` adjusts either.
JUnit covers the API against a running server.

Regression rules that land here: resetting the statistics does not break the
population history.

On the screen: an agent starts `serve --no-browser --port 0`, reads the port
from the ready line, and with `curl` creates a world, spawns rabbits, steps it
500 days and reads the snapshot, the census and the log back.

Depends on entry 1. See
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0010](decisions/0010-runs-replay-exactly-from-a-seed.md),
[0020](decisions/0020-the-interface-is-a-web-page.md),
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).

## 3. Watching in a browser

Status: not started.

The frontend is React with TypeScript and Vite, built into the jar by
`frontend-maven-plugin`, and `serve` opens it in the browser. A PixiJS canvas
draws the grid with a rabbit sprite from a CC0 asset pack, glides each rabbit
between its tiles by diffing snapshots by id, fades rabbits in and out, and
shows a count on a tile holding more than one, drawing the lowest-id occupant. A
snapshot whose seed changed or whose day went backwards starts a new run, so
nothing glides across a reset. The controls are step, play and pause, and
playback rate; spawning rabbits by count and by clicking a tile; resetting the
world, which draws a new seed from the clock and shows it; creating or resetting
a world sends the starting spawns, ten rabbits, since a world starts empty; a
fast-forward that steps ahead; a picker that lists the server's worlds and
switches, creates or deletes them, with the world's id in the page's address so
a reload returns to it; and the population chart with a statistics reset, which
fills a gap in the stream from the census history. Vitest covers the frontend's
diffing, gliding and fading.

2022 features that come back here: spawning by count and by click, stepping and
automatic play with a rate, resetting the world, and the population graph with
a statistics reset.

Details a review found open, to settle in this entry's grill: the browser's
limit of six connections to one origin; pinning Node and npm packages, with
`package-lock.json`, `npm ci` and Dependabot for npm; skipping the frontend
build for Java-only work; and the chart library.

On the page: a plain ground of 20 by 20 tiles with ten rabbits that glide
between tiles when you press play, fade in when spawned, and show a count where
they share a tile, beside the population chart and the current seed.

Depends on entry 2. See
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0020](decisions/0020-the-interface-is-a-web-page.md).

## 4. Checking the page

Status: not started.

A project skill with which an agent opens the real page in a browser, uses the
controls, takes screenshots and checks what it sees against the snapshot from
the API, per `docs/ideas/browser-smoke-test.md`. It runs on demand when a change
touches the page, and not in CI. It replaces Elias watching as the check in the
implementing-roadmap-entries skill, with Elias still judging how it looks.

The skill gets an `evals/` scenario like every workflow skill, per
[0019](decisions/0019-workflows-are-skills-and-invariants-are-scripts.md).

On the screen: an agent reports, with screenshots, that the rabbits it spawned
are drawn on the tiles the snapshot puts them on, and that pressing play moves
them.

Depends on entry 3. See
[0019](decisions/0019-workflows-are-skills-and-invariants-are-scripts.md),
[0020](decisions/0020-the-interface-is-a-web-page.md).

## 5. Eating and starving

Status: not started.

Grass as an `Edible` entity, and a world process that spawns it. The feed and
metabolise steps and the list of world processes join the day here. `Metabolism`
holds a reserve its carrier can see and its neighbours cannot. The feed step
takes every edible on the tile, and the metabolise step charges on an interval
and removes anything whose reserve falls below zero. Intervals and charges join
the settings as species settings, and how much food arrives and how often join
as world settings, with bounds that reject non-positive values. `Setting`
arrives here with its first shape, which this entry's grill settles. That is
where the regression rule rejecting a zero eating interval lands, and the rule
that removing an entity validates before it mutates, since starvation is the
first thing that removes one.

Rabbit gets the fixed-rules brain that approaches the nearest edible. Its rule
set is a design decision per
[0018](decisions/0018-brain-computation-model-and-warm-starting.md), so it gets
a log entry before it is built. Its parameters sit in the baseline genome as
plain values until entry 6. The rule set's log entry also settles whether
walking away from a crowd survives, which decides whether the inverted crowd
comparison rule lands or goes to `docs/ideas/`.

A creature standing on grass is now the normal case, so the grid draws a
creature above the ground cover it stands on, and the count from entry 3
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

Depends on entry 3. See
[0009](decisions/0009-terrain-is-entities-plus-tile-fields.md),
[0013](decisions/0013-perception-is-one-type.md),
[0016](decisions/0016-feeding-takes-the-tile.md),
[0017](decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 6. Breeding and settings

Status: not started.

`Breed` as an intent, with interval, cost and endowment as settings. `GeneSpec`
gains scalar and simplex mutation, which turns the baseline values from entries
1 and 5 into genes that vary and pass to a child: the step distribution, view
range and the fixed-rules brain's parameters, plus speed. Speed arrives with its
metabolism charge, since a free speed gene pins to its bound. `run` reports gene
means.

2022 features that come back here: breeding on an interval when the reserve
covers the cost, every fifteen days at a cost of three by default, with the
child on its parent's tile; and mutation of every inherited trait. A child
starts from an endowment rather than the 2022 empty reserve.

Regression rules that land here: the step variation is not integer divided, the
step distribution stays valid, view range is inherited and varied, a view range
never drops below one, and a zero breeding interval is rejected.

Then the parameters panel, which 2022 also had, generated from declared
settings, with editing a setting as a command. This entry's grill settles what
a world reset does with edited settings: keeping them means the new log no
longer replays from the seed alone, and reverting them loses the edits. It also
settles which settings can only be set when a world is created.

On the page: a newborn appears on its parent's tile, the population grows and
shrinks on its own, and a settings panel changes any declared number while the
world runs.

Depends on entry 5. See
[0002](decisions/0002-replace-the-model-layer-in-place.md),
[0004](decisions/0004-creatures-return-intents.md),
[0006](decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0017](decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 7. Rabbit learns

Status: not started.

A gate rather than a feature: selection has to be shown working before a second
species muddies it. A world setting turns mutation off, and the runner compares
mutating runs against that control across several seeds. `run` gains what that
needs: overriding any declared setting from the command line or a file, and a
batch over a range of seeds in one process, run in parallel, with a summary line
per seed.

The entry is done when mutating runs reliably outlast or outnumber the control,
the brain's genes drift the same way across seeds, and speed settles short of
its bound. This entry's grill turns each of those into a number, such as
mutation winning in at least eight of ten seeds by day 1000, and sets a timebox
for tuning. It also settles whether a gene's mutation size can be overridden
like a setting, since tuning it should not need a rebuild. Getting there is
tuning the default settings and food pressure until differences between rabbits
matter. If behaviour stays dull, widen what a rabbit perceives or can do before
reaching for the network brain, per `DESIGN.md`.

On the page: nothing new is required. What changes is behaviour you can see,
such as rabbits heading for food more directly after a few hundred days than at
the start.

Depends on entry 6. See
[0012](decisions/0012-tests-target-brains-without-a-world.md),
[0014](decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0018](decisions/0018-brain-computation-model-and-warm-starting.md),
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).

## 8. Predator

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

Details to settle in this entry's grill: whether a predator can catch anything.
Every creature decides before anyone moves, and a move is a direction and a
distance rather than a target, so a wolf walks toward where a rabbit was, and a
fleeing rabbit as fast as the wolf may never be caught. The ways out are an
intent that names its target and is resolved against where the target stands
when the resolver reaches it, or deciding on each creature's turn, which
[0005](decisions/0005-a-day-is-one-method.md) names as a change to one method.
Either changes the shape of a day or an intent, which `DESIGN.md` counts as a
design flaw to record, so the grill settles it before the hunting brain is
written.

On the page: a wolf with its own sprite, rabbits running from a wolf they can
see, and a kill that removes a rabbit at once.

Depends on entry 7. See
[0004](decisions/0004-creatures-return-intents.md),
[0005](decisions/0005-a-day-is-one-method.md),
[0013](decisions/0013-perception-is-one-type.md),
[0016](decisions/0016-feeding-takes-the-tile.md).

## 9. Inspecting creatures

Status: not started.

Looking at one creature closely. Clicking a tile shows what stands on it.
Selecting a creature shows its age in days, its reserve and its genes, and
draws its view range on the grid. A list of every creature sorts by age or by
reserve. The selection clears when the selected id stops resolving, per
[0009](decisions/0009-terrain-is-entities-plus-tile-fields.md).

2022 features that come back here: a creature's age, the list of humans sorted
by days survived or by food, selecting a human to see its information and view
range, and clicking a tile to see its contents.

API tests cover what a selection returns, and that a selection whose id has
stopped resolving comes back empty rather than failing.

On the page: clicking a rabbit shows its age, reserve and genes and outlines
the tiles it can see, and the list sorts by age or reserve.

Depends on entry 6. See
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0013](decisions/0013-perception-is-one-type.md),
[0020](decisions/0020-the-interface-is-a-web-page.md).

## 10. Looking good

Status: not started.

The pass that serves the first goal in `DESIGN.md`. Art in one consistent style
replaces the placeholder asset pack for every species and the ground. The page
gets a designed layout and theme, light and dark. The smooth movement from entry
3 becomes a hop, and births, deaths and kills get small effects. An effect that
a diff of two snapshots cannot see, such as which creature killed which, needs
events on the day's report, per
[0020](decisions/0020-the-interface-is-a-web-page.md).

The entry is done when someone who has never seen the project wants to keep
watching, which is judged by watching. Underneath that, a checklist: art for
every species and the ground, both themes, the hop, an effect for each of
birth, death and a kill, and the page holding 60 frames a second with 200
creatures on an ordinary laptop.

On the page: everything above.

Depends on entry 8, so every species that exists by then gets its art. See
[0020](decisions/0020-the-interface-is-a-web-page.md).

## 11. Hosting

Status: not started.

The app as a link anyone can open. Each visitor's worlds belong to their
session, abandoned worlds are cleaned up, and a cap on worlds across sessions
keeps a spike in traffic from exhausting the machine. A container image and a
deploy workflow put it on Fly.io.

API tests cover that a second session can neither see nor delete the first's
worlds, that the cap refuses a new world once reached, and that an abandoned
world is removed after its timeout.

Details to settle in this entry's grill: how many worlds a session may hold, the
cap across sessions and the idle timeout, sized against what a world costs in
memory and threads; what the page shows a visitor when a cap is reached;
limiting the rate of requests per session, since a stranger can step and play
worlds as fast as the server allows; and how cleaning up abandoned worlds fits
the rule from entry 2 that a local server never deletes a person's world.

On the screen: the link opens the page with a fresh world, and two browsers
opening it each get their own.

Depends on entry 6, and comes after entry 10 so the first link strangers open
shows the finished look. See
[0020](decisions/0020-the-interface-is-a-web-page.md),
[0021](decisions/0021-agents-drive-worlds-over-http-and-run.md).
