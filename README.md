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
