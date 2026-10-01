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

The browser's grid renderer only. The snapshot already carries every entity on
every tile with its displayable values, so nothing in the model changes.

Roadmap entry 2 draws the lowest-id occupant with a count when there is more
than one, so the grid and the population chart agree. This idea is whatever
goes beyond that.

## Open questions

What to draw beyond the count: several sprites shrunk into the tile, or a tint
that gets stronger with occupancy.

Whether the choice is per species, so grass under a creature reads as ground
rather than as a competing occupant.
