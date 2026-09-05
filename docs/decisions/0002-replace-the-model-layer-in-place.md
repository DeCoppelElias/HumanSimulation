# 0002. Replace the model layer in place, growing from zero

## Status

Accepted, 2026-09-03.

## Context

The model layer cannot grow. The world knows every content type by name and
branches on it in a dozen places, a creature's life is a fixed script, boundary
rules live inside the agent, and the genome is constructor parameters. Adding a
wolf means editing the world, the tile, the renderer and three behaviour classes.

Growing the new core in a package beside the old one was the alternative, and it
had two arguments. A headless runner could measure both cores on the same seed.
And the nine regression scenarios could be ported one at a time and checked
against a version that still demonstrably passes them, so there would always be a
working oracle.

The first argument dissolved when the comparison turned out not to be wanted: a
before and after gif is enough. The second dissolves differently, because there
is no porting. Growing from zero means each rule arrives with a fresh test
written against the new units, so the old suite is never the thing being
satisfied and an oracle has nothing to do.

## Decision

Replace in place, on a branch, with `master` holding a working application until
the switchover lands.

Build up from zero rather than porting the current model wholesale. The first
package is thin and complete: a grid, one species, a random brain, a move intent,
a resolver, a snapshot, and the interface drawing it. Capabilities are added one
at a time after that, each package ending with something watchable.

What survives: `GridPosition` as a value type, `LineChart` untouched,
`DataAnalytics` reading a census, most of the interface panels, and the
`GridPositionTest`, `DeterminismTest` and `ResourceLoadingTest` suites.
Everything else under `SimulationApplication` is replaced, `MovementAction`
included, since it holds a mutable delta pair where
[0004](0004-creatures-return-intents.md) needs a direction and a distance.

## Consequences

Git provides the reference, since the old core is one checkout away and the 2022
original is on `v1.0-original-2022`. What it does not provide is two cores
running in one process on one seed, so any comparison is a matter of running one,
recording numbers, and running the other.

The nine model-bug rules become acceptance criteria on the package that
reintroduces each rule, per [0012](0012-tests-target-brains-without-a-world.md).
The food contest package carries the fight-winner rule, the metabolism package
carries the interval validation.

Behaviour rules that were never bugs need the same treatment and are easier to
lose, since nothing in the tree names them. The food contest rule, meaning one
aggressive contender takes the food, several means one wins from the seeded
generator and the rest die, and none means the peaceful ones split it evenly, is
the clearest example. The work plan owns that list.

Each package is a live test of the claim in `DESIGN.md`.

The application is broken on the branch for as long as the rebuild takes. Growing
vertically keeps the window short.
