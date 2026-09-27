# 0016. Geometry lives in the grid

## Status

Accepted, 2026-09-27.

## Context

[0013](0013-perception-is-one-type.md) measures a perception by straight-line
distance and [0004](0004-creatures-return-intents.md) moves by a direction and a
distance. On a square grid those two disagree, and each fix picks a different
lie. Four directions means movement is Manhattan while sight is Euclidean, so a
creature sees a corner tile it cannot reach in one move. Eight directions means a
diagonal step covers 1.41 tiles for the price of one, so anything moving toward a
target evolves to prefer diagonals.

A hexagonal grid removes the mismatch rather than choosing a lie, because all six
neighbours sit at the same distance and hex distance is a proper metric. Its cost
is not in the model. Axial coordinates replace x and y, so `GridPosition` stops
being one of the few types
[0002](0002-replace-the-model-layer-in-place.md) keeps, and `GridPanel` has to
draw hexagons and hit-test mouse clicks against them, which is the fiddliest code
in the project for the smallest reward.

## Decision

Four directions, as the 2022 model has: north, south, east and west.

The grid owns the direction set and the distance function. Nothing outside the
grid enumerates directions or measures distance. A brain picks a direction from
what the grid offers or from what it perceives, and asks the grid how far
something is.

## Consequences

Changing the shape of the world is a contained change, kept in
`docs/ideas/hexagonal-grid.md` rather than closed off.

The mismatch stands in the meantime. Sight is straight-line and movement is
Manhattan, so a corner tile is visible and two steps away.

No code switches on a direction, which costs a little indirection everywhere a
direction is used, and means a brain cannot hardcode north.

A gene holding a distribution over directions would have its length tied to the
shape of the grid, so the step distribution over distances is the safer of the
two to evolve. Nothing needs a direction distribution today.
