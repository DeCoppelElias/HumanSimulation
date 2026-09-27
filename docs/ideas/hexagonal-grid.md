# Hexagonal grid

Status: idea.

## What it does

Replaces the square grid with a hexagonal one. Six neighbours, all at the same
distance, so hex distance is a proper metric.

## Why it is interesting

It removes a mismatch rather than papering over it. On a square grid, sight is
straight-line and movement is not, so with four directions a creature sees a
corner it cannot reach in one move, and with eight a diagonal step covers 1.41
tiles for the price of one. On hexes, one step and one unit of distance are the
same thing everywhere.

## What it would touch

Not much of the model, because
[0016](../decisions/0016-geometry-lives-in-the-grid.md) keeps the direction set
and the distance function inside the grid. The cost is in the interface. Axial
coordinates replace x and y, so `GridPosition` stops being one of the types
[0002](../decisions/0002-replace-the-model-layer-in-place.md) keeps, and
`GridPanel` has to draw hexagons and hit-test mouse clicks against them.

## Open questions

Whether anything in the simulation is actually being held back by the square
grid, or whether it is a correctness itch.

What a gene holding a distribution over directions does when the direction count
changes, since its length is then tied to the shape of the grid.
