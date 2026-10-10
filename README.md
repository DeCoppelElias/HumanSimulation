# HumanSimulation
[![CI](https://github.com/DeCoppelElias/HumanSimulation/actions/workflows/ci.yml/badge.svg)](https://github.com/DeCoppelElias/HumanSimulation/actions/workflows/ci.yml)

A world you watch evolve. Rabbits wander a grid today. Later versions let them
eat, breed and pass varied behaviour to their children, so the population
drifts toward whatever works. `DESIGN.md` says where it is going and
`docs/roadmap.md` how far it has got.

The 2022 version, a Swing app of humans looking for food, is on the
`v1.0-original-2022` tag.

## Building

Java 25. No Maven install is needed, since the wrapper fetches it.

    ./mvnw verify     compile, check formatting, run the tests
    ./mvnw package    also build target/HumanSimulation.jar

## Running

There is no window yet. `run` advances a seeded world and prints its census as
JSON Lines, one line per day.

    java -jar target/HumanSimulation.jar run --seed 42 --days 100
    {"seed":42,"day":1,"population":{"rabbit":10}}
    {"seed":42,"day":2,"population":{"rabbit":10}}

`--days` is required. `--seed` defaults to one drawn from the clock, `--width`
and `--height` to 20, and `--spawn rabbit=10` sets the starting population. The
same seed prints the same lines.

## Serving

`serve` holds worlds behind an HTTP API, for an agent today and for the page
once it exists. It prints one line on stdout when it is ready and logs to
stderr.

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
it on a timer, and `GET .../events` streams each day. `docs/roadmap.md` lists
every route. With `--no-browser` the server quits after 10 minutes without a
request, and without it never quits. It listens on `127.0.0.1` unless `--host`
says otherwise.
