# Golden-run test

Status: idea.

## What it does

A test runs `run --seed 42 --days 100` in-process and compares its output byte
for byte against a checked-in file. A documented switch regenerates the file,
and a commit that does so says which rule changed the run.

## Why it is interesting

`DeterminismTest` checks that one build replays its own run. Nothing checks
that a run stays the same from one build to the next, so a refactor that moves
one draw from the world's generator changes every seeded run while every test
passes.

## What it would touch

One test and one file under `src/test/resources`, plus a line in `AGENTS.md`
on when regenerating is allowed.

## Open questions

Whether it is worth its overhead, which is why it was left out of roadmap entry
1. It breaks on every intended behaviour change: each rule in entries 5 and 6
and each tuned default in entry 7, so regenerating it becomes part of almost
every change. What it catches is mostly harmless, since comparisons across
changes are about the direction of selection over many seeds rather than a
matching trace, per [0012](../decisions/0012-tests-target-brains-without-a-world.md),
and a run that drifted is as valid as the one before. Pinning only the final
day's census has the same cost.

What would change the answer is silent drift causing a real problem, which the
retrospective at the end of an entry would surface.
