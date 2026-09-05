# 0009. Terrain is entities plus tile fields

## Status

Accepted, 2026-09-03.

## Context

Terrain has to carry state, and both pure answers fail.

Terrain as tile state alone means fire needs one code path for burning entities
and a second for burning tile state, and every future capability faces the same
fork.

Terrain as entities alone means inventing an object to hold a number that belongs
to the location. Elevation is not a thing sitting on a tile.

An earlier form of the split asked two questions: can there be more than one on a
tile, and can it be removed. That misclassifies water, which is one per tile and
is nonetheless an entity carrying a blocking capability. The first question was
doing the wrong work.

## Decision

One question separates the two. Can the thing be created and destroyed
independently of the tile?

Grass, water, rock and ash all can, so they are entities. Grass carries an edible
component and a flammable one, water carries a blocking one. A tile always has
exactly one elevation and it cannot be detached, so elevation is a field,
alongside moisture and temperature.

Fire is neither. It is a state of something that occupies a tile, so it is a
component attached to that thing.

## Consequences

Fire burns grass, humans and wolves through one code path, with no species-level
special case.

A fully covered world is one entity per tile, which is nothing at a few hundred
tiles and real work to revisit at tens of thousands. This is the most
scale-sensitive decision in the log.

Asking whether a tile is passable means iterating its occupants and checking
components, which the resolver does per step, per creature, per day, where a
field would be one boolean read.

Burnt grass comes back with a new id, because burning out despawns and respawns.
Anything holding an id across days sees the old one vanish, which is correct for
the census and means the interface must clear its selection when the selected id
stops resolving.
