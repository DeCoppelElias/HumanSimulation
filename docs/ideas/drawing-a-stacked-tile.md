# Drawing a stacked tile

Status: idea.

## What it does

Shows what is actually on a tile when several things share it.
[0004](../decisions/0004-creatures-return-intents.md) puts no limit on
occupancy, so a tile can hold grass, three creatures and a fire, and the grid
draws one sprite.

## Why it is interesting

Crowding is a real force in the simulation, felt through food running out, and
it is currently invisible. A watcher cannot see the thing that is killing the
population.

## What it would touch

`GridPanel` only. The snapshot already carries every entity on every tile with
its displayable values, so nothing in the model changes.

## Open questions

What to draw: the top sprite with a count, several sprites shrunk into the tile,
a badge, or a tint that gets stronger with occupancy.

Whether the choice is per species, so grass under a creature reads as ground
rather than as a competing occupant.
