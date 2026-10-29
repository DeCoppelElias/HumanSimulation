# Run speed

Status: idea.

## What it does

Profiles a headless run and removes its hot spots, so a seeded run of the same
world finishes sooner and prints the same lines.

## Why it is interesting

A 40×40 predator run of 2000 days takes about 13 seconds, about 7 ms a day for
around a hundred animals, and an 80×80 run of 1000 days about 33 seconds. Every
probe, gate and tuning budget is a multiple of that, and entry 12's tuning is
held to runs of under 30 minutes on half the cores of the machine it runs on.

Two costs are visible without a profiler. `World.advance()` builds a whole-world
snapshot every day, every tile and a copy of every genome, and `run` reads it
only on export days. A perception is allocated per creature per day, which
[0013](../decisions/0013-perception-is-one-type.md) already names as internal
to the builder if it ever matters.

## What it would touch

`World`, `RunCommand` and `CircularSense`, and nothing a brain or a test sees.
`DeterminismTest` and `ReplayAcrossProcessesTest` hold the output fixed.

## Open questions

Which cost dominates, which a profile answers before anything changes.

Whether the snapshot becomes lazy inside `DayReport` or `run` asks for one only
on the days it needs, since `serve` streams one every day.
