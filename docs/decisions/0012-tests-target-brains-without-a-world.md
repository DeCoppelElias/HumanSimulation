# 0012. Tests target brains without a world

## Status

Accepted, 2026-09-03.

## Context

The existing suite was written against classes that are being replaced. It says
useful things about what the units should be, so what happens to each group is
worth deciding once.

Three groups behave differently. Determinism, resource loading and grid geometry
test rules rather than structure. Behaviour mechanics, meaning movement
distributions, breeding and the food contest, test units that are changing shape.
The nine model-bug regression cases encode bugs that took a full phase to find.

Selection is the part no unit test can see. A rebuild can compile, pass
everything, and quietly stop selecting for anything.

## Decision

Determinism, resource loading and grid geometry carry over with new type names.

Behaviour mechanics are rewritten against the new units. A brain is tested by
handing it a perception and asserting the intent it returns, with no world at
all.

The nine regression rules become acceptance criteria on the package that
reintroduces each rule, from [0002](0002-replace-the-model-layer-in-place.md),
rather than a suite ported in one go. All nine carry over; none is written off.

A headless runner is built early. It advances a seeded world for a given number
of days and reports population and gene means.

## Consequences

The runner is the only thing that catches a build which passes every unit test
and no longer selects. It is also what records the before and after of the
rebuild, alongside the `gui-smoke-test` skill for what has to be watched rather
than measured.

The 2022 selection baseline no longer applies, because the previous phase fixed
the dead view-range assignment and changed how the simulation evolves. Any
comparison is about the direction of selection rather than a matching trace,
since [0004](0004-creatures-return-intents.md) changes movement deliberately.

Nine rules spread across nine packages is nine chances to forget one. A suite
ported in one go would have failed loudly instead, and the work plan is now the
only thing tracking that each rule found a home.
