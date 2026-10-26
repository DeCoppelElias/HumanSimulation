# HumanSimulation
[![CI](https://github.com/DeCoppelElias/HumanSimulation/actions/workflows/ci.yml/badge.svg)](https://github.com/DeCoppelElias/HumanSimulation/actions/workflows/ci.yml)

A world you watch evolve. Grass falls on a grid every seven days, and rabbits
walk toward the grass they can see, eat it, and starve when they go too long
without. A rabbit with enough reserve can breed, and its child inherits its
genes with small mutations, so the population drifts toward whatever works.
`DESIGN.md` says where it is going and `docs/roadmap/` how far it has got.

The 2022 version, a Swing app of humans looking for food, is on the
`v1.0-original-2022` tag.

## Building

Java 25. No Maven install is needed, since the wrapper fetches it.

    ./mvnw verify     compile, check formatting, run the tests
    ./mvnw package    also build target/HumanSimulation.jar

The page is built from `frontend/` in the same run. The build downloads its own
Node into `target/`, so none is needed on PATH. `-Pskip-frontend` builds the
Java alone, and that jar serves the API without a page. While working on the
page, `./mvnw frontend:npm "-Dfrontend.npm.arguments=run dev"` serves it with
hot reload on port 5173, passing API calls to a `serve` on port 7070.

## Watching

    java -jar target/HumanSimulation.jar serve

`serve` opens the page in your browser. Pick a world or create one, with a seed
or one drawn from the clock, and it starts with ten rabbits and seven grass on a
20 by 20 grid. Play, pause, step, change the speed or fast-forward up to 10,000
days. Pick a species beside the spawn count, then spawn that many or click a
tile to drop one there. Grass draws under a rabbit standing on it, and a tile's
number counts its rabbits. A chart beside the grid follows each species. The
address holds the world's id, so a reload or a second tab shows the same world.

Under the chart, the settings panel has a slider for every number the world
reads, grouped as World, Grass and Rabbit, each with its description: how much
grass falls and how often, what a rabbit pays a day, how far it walks, and what
breeding costs. An edit takes effect at the next day and only in that world. An
edited setting is marked in bold, its ↺ button puts the default back, and
"Restore all" resets every one. A reset keeps the edits. The World group starts
with a `mutation` checkbox. Untick it and children and new spawns become exact
copies, so the world stops evolving.

The sprites are from [Kenney](https://kenney.nl), CC0: the rabbit from Animal
Pack Remastered and the grass from Foliage Pack.

## Running

`run` advances a seeded world without a page and prints its census as JSON
Lines, one line per day: the population of each species, and the mean of each
gene over the members alive that day.

    java -jar target/HumanSimulation.jar run --seed 42 --days 300
    {"seed":42,"day":1,"population":{"grass":0,"rabbit":10},"geneMeans":{"rabbit":{"breedPull":1.01,...,"viewRange":2.99,"whim":0.53}}}
    ...
    {"seed":42,"day":300,"population":{"grass":1,"rabbit":10},"geneMeans":{"rabbit":{"breedPull":1.12,...,"viewRange":4.01,"whim":0.53}}}

`--days` is required. `--seed` defaults to one drawn from the clock, `--width`
and `--height` to 20. `--spawn SPECIES=COUNT`, once per species, sets the
starting population, which is ten rabbits and no grass without it. Grass falls
on its own every seven days either way. The same seed prints the same lines.

`--set OWNER.KEY=VALUE` changes any setting from day 0, and
`--set-at DAY:OWNER.KEY=VALUE` changes it once DAY days are complete, so a run
can get harsher as it goes. `--gene SPECIES.GENE=VALUE` starts that species'
spawn from another value of one gene. Each is repeatable, and a bad name or
value fails with the same message the panel would give.

    java -jar target/HumanSimulation.jar run --seed 1 --days 500 --set world.mutation=0 --set-at 200:world.grassPerDrop=3 --gene rabbit.grassPull=0.2

## Comparing

`tools/compare.py` runs variants of a world over many seeds, one `run` per seed
and variant in parallel, and reports how each fared. It needs the jar from
`./mvnw package` and Python 3, and nothing else.

    python tools/compare.py tools/experiments/mutation-ramp.json
    python tools/compare.py --seeds 1-30 --days 500 --variant base= --variant cheap="--set rabbit.speedCharge=0.05"

A preset in `tools/experiments/` names the seeds, days, shared flags and the
variants, and flags on the command line override it. `watchable` is the world
the page starts from. `mutation-ramp` runs mutating rabbits against clones as
food runs short.

Per species, the report gives the day each world died out, its population, and
how far each gene's mean moved from day 1. It then compares each variant with
the first on every measure: both medians, Â12 (the chance that a run of the
variant beats a run of the first, 0.5 meaning no difference), and a
Mann-Whitney p printed twice. Read the raw p for a measure you chose before
running, and the Holm p, which allows for every test in the report, when
scanning for anything that stands out. `--per-seed` adds each seed's genes and
`--json` prints everything. The script never says which variant won.

## Serving

`serve` holds worlds behind an HTTP API, which the page and an agent both use.
It prints one line on stdout when it is ready and logs to stderr.
`--no-browser` leaves the browser closed, which is how an agent runs it.

    java -jar target/HumanSimulation.jar serve --no-browser --port 0
    {"event":"ready","url":"http://127.0.0.1:63214","port":63214}

A world starts empty and paused. Create one, spawn rabbits, step it ahead and
read it back:

    curl -X POST localhost:63214/worlds -d '{"seed":42}'
    curl -X POST localhost:63214/worlds/ID/commands -d '{"type":"spawn","species":"rabbit","count":10}'
    curl -X POST localhost:63214/worlds/ID/step -d '{"days":500}'
    curl localhost:63214/worlds/ID/census?from=498
    curl localhost:63214/worlds/ID/log

`GET /worlds/ID` is the current snapshot, `POST .../play` and `.../pause` run
it on a timer, and `GET .../events` streams each day. `docs/roadmap/02-serving-worlds-over-http.md`
lists the first routes. `GET .../settings` lists every declared setting with its
bounds, step and default, and the snapshot carries the current values. Two
commands change them:

    curl -X POST localhost:63214/worlds/ID/commands -d '{"type":"setSetting","owner":"rabbit","key":"breedingLoss","value":20}'
    curl -X POST localhost:63214/worlds/ID/commands -d '{"type":"restoreDefaults","owner":"rabbit"}'

`restoreDefaults` with no owner restores everything, and with an owner and a
`key` one setting. A spawn takes an optional `genes` object, such as
`{"grassPull":0.2}`, which overrides those genes for the members it creates. Process settings belong to the owner `world`. With `--no-browser` the server quits after 10 minutes without a
request or an open stream, and without it never quits. It listens on `127.0.0.1` unless `--host`
says otherwise.
