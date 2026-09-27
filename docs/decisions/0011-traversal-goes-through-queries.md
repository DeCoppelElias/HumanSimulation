# 0011. Traversal goes through queries

## Status

Accepted, 2026-09-03. Revised 2026-09-27.

## Context

Large worlds are not a current target, and several decisions here are justified
by the current size. The question is which of them close the door on ever
growing.

Most do not, provided nothing reaches around the world to touch its collections.
An index makes component lookups fast, a spatial structure makes perception
fast, and a version-based diff makes snapshots small. Each is an implementation
detail of one method.

The alternative is letting systems walk the entity collection and the grid
directly, which is what the code does today. It is less machinery, it needs no
query surface designed in advance, and it makes every system's cost obvious at
the call site instead of hidden behind a method name. It also means that the day
any of those optimisations is wanted, every system is a caller that has to
change.

Splitting the entity type was the other way to make the decide step cheap: an
`Actor` that always has a brain and a `Prop` that never does, each in its own
collection. It runs into two settled rules. Asking a tile what stands on it has
to come back in one ascending id order across every occupant, per
[0010](0010-runs-replay-exactly-from-a-seed.md), and a creature and the grass it
stands on often share a tile, so two collections mean merging by id on every
such query, or a third sorted view that is an index by another name. And a brain
can be added to anything, per [0003](0003-entities-carry-components.md), which
under a split becomes moving an entity between two Java types.

## Decision

No system iterates the entity collection or the grid directly. Every traversal
goes through a query on the world or the grid.

A query returns a fresh list rather than a view of the world's collections. A
system can therefore remove or spawn entities while walking the result, which
removal at death in [0004](0004-creatures-return-intents.md) needs, and an id in
the list that no longer resolves when its turn comes is skipped.

The entity model stays one type. When a scan over all entities becomes a real
cost for the decide step or for a component query, the world builds an index
behind the same query surface: a list of the ids that carry a `Brain`, and one
per component for whichever queries turn out to matter, kept up to date on spawn
and removal rather than rebuilt by scanning. `world.entitiesWithBrain()` and
`world.query(SomeComponent.class)` keep their signatures whichever answers them.

## Consequences

An index, a spatial structure or a partial snapshot can be added later without a
caller changing. There is still exactly one canonical, id-sorted entity list,
and an index is a cached view into it, so the ordering guarantee costs nothing
extra. No index exists yet, and none is needed at the scale `DESIGN.md` targets.

The compile-time proof that a creature has a brain, which
[0003](0003-entities-carry-components.md) gives up, stays given up. The
`Actor`/`Prop` split is rejected only as a fix for query cost. If that proof is
ever wanted for its own sake, the split is still the way there, at the cost
above.

The query surface has to be designed before the first system is written, and
guessed at while there are few systems to learn from. A query nobody needs is
dead weight, and a missing one gets worked around.

The abstraction hides the cost it is meant to bound. Asking the world for
everything with a given component is a full scan wearing the name of a lookup,
so a system's real cost stops being visible where it is paid.

Nothing enforces the rule. It is checkable by reading and by nothing else.

Every traversal allocates a list. At a few hundred entities per system per day
that is nothing.

Two scale costs are untouched by it, and both are named where they are made:
ground cover as entities in
[0009](0009-terrain-is-entities-plus-tile-fields.md), and a day being a full
pass in [0005](0005-a-day-is-an-ordered-list-of-systems.md).
