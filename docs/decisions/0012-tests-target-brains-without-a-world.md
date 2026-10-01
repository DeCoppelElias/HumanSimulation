# 0012. Tests target brains without a world

## Status

Accepted, 2026-09-03. Revised 2026-10-01.

## Context

The existing suite was written against classes that are being replaced. It says
useful things about what the units should be, so what happens to each group is
worth deciding once.

Three groups behave differently. Determinism, resource loading and grid geometry
test rules rather than structure. Behaviour mechanics, meaning movement
distributions, breeding and the food contest, test units that are changing
shape. The nine model-bug regression cases encode bugs that took a full phase to
find.

The bug fix phase wrote every regression test first and watched it fail before
changing the code, and that caught a test that would have passed vacuously.
Writing tests after the code was the alternative. It is faster per change and
produces tests that confirm whatever the code already does.

Selection is the part no unit test can see. A rebuild can compile, pass
everything, and quietly stop selecting for anything.

## Decision

Development is test-driven. A rule's test is written first, run, and seen to fail
for the reason it names before the code that makes it pass exists. Tests drive
the domain through its own surface, commands in and snapshots out, or a brain
handed a perception, and never through an adapter.

Determinism and grid geometry get fresh tests against the new types, since no
2022 code survives, per [0002](0002-replace-the-model-layer-in-place.md).
Resource loading guarded the 2022 icons at the classpath root and goes with
them. The web adapter's tests cover what it serves.

Behaviour mechanics are rewritten against the new units. A brain is tested by
handing it a perception and asserting the intent it returns, with no world at
all.

Eight of the nine regression rules become acceptance criteria on the package
that reintroduces each rule, from
[0002](0002-replace-the-model-layer-in-place.md), rather than a suite ported in
one go. The ninth, that a food fight leaves exactly one winner, has nothing to
guard, since there is no contest in [0016](0016-feeding-takes-the-tile.md), and
the rule it guarded is kept in `docs/ideas/aggression-at-contested-food.md`.

A headless runner is built early, as the `run` command from
[0021](0021-agents-drive-worlds-over-http-and-run.md). It advances a seeded
world for a given number of days and reports population and gene means.

## Consequences

The runner is the only thing that catches a build which passes every unit test
and no longer selects. It measures the new core across changes, alongside a
person watching the page for what has to be seen rather than measured. The
before and after of the rebuild is a gif, per
[0002](0002-replace-the-model-layer-in-place.md).

The 2022 selection baseline no longer applies, because the current code already
differs from 2022 in how view range is inherited. Any comparison is about the
direction of selection rather than a matching trace, since
[0004](0004-creatures-return-intents.md) changes movement deliberately.

Each rule arrives with a test that has already failed once, so the suite
documents behaviour the code was made to meet rather than behaviour it happened
to have.

Eight rules spread across their packages are eight chances to forget one. A
suite ported in one go would have failed loudly instead. Each rule is an
acceptance line on the roadmap entry that rebuilds it, and the roadmap is what
tracks that every rule found a home.
