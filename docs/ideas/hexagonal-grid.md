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
[0015](../decisions/0015-geometry-lives-in-the-grid.md) keeps the direction set
and the distance function inside the grid. The cost is mostly in the interface.
Axial coordinates replace x and y in every position, and the browser's grid
renderer has to draw hexagons and hit-test clicks against them.

## Open questions

Whether anything in the simulation is actually being held back by the square
grid, or whether it is a correctness itch.

What a gene holding a distribution over directions does when the direction count
changes, since its length is then tied to the shape of the grid.
