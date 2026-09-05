# 0014. Decide and queries get an index, not a type split

## Status

Accepted, 2026-09-05.

## Context

[0003](0003-entities-carry-components.md) gives every entity, brained or not, the
same shape, and names the cost without paying it: finding everything with a given
component, or everything with a brain, is a scan with a filter.
[0011](0011-traversal-goes-through-queries.md) reserves the fix — "an index, a
spatial structure or a partial snapshot can be added later without a caller
noticing" — but does not say what that fix looks like.

The alternative is to split the entity type itself: an `Actor` that always has a
brain and a `Prop` (or similar) that never does, each in its own collection, so
decide iterates the actor list directly and never touches grass, water or ash.

That split runs into two invariants already settled.
[0010](0010-runs-replay-exactly-from-a-seed.md) requires that asking the grid
what stands on a tile comes back in one ascending-id order across every
occupant, actor or not. A wolf and the grass it stands on are frequently on the
same tile, so two collections mean merging by id on every such query — the same
traversal the split was meant to avoid, or a third, unified, sorted view that is
the index by another name. And [0008](0008-brains-decide-systems-apply.md)
already allows a brain to be added to anything, water included, as an odd but
unforbidden case; under a split, that stops being filling in a field and becomes
moving an entity between two Java types.

## Decision

The entity model does not change. When a scan-and-filter over all entities
becomes a real cost for the decide step or for a system's component query, the
world builds an index behind the existing query surface: for instance an
incrementally maintained list of ids that currently carry a `Brain`, and
per-component indices for whichever queries turn out to matter, updated on spawn
and despawn rather than rebuilt by scanning. `world.entitiesWithBrain()` and
`world.query(SomeComponent.class)` keep the same signature whether a scan or an
index answers them, so no caller changes when the switch is made.

## Consequences

There is still exactly one canonical, id-sorted entity list. An index is a
cached view into it, so [0010](0010-runs-replay-exactly-from-a-seed.md)'s
ordering guarantee costs nothing extra to keep.

The type safety [0003](0003-entities-carry-components.md) already gave up —
nothing stops a species being built with a metabolism and no way to eat — stays
given up. This entry rejects the `Actor`/`Prop` split only as a fix for query
cost; if compile-time proof that an actor has a brain is wanted for its own sake
later, that split is still the way there, and pays the same id-order and
brain-mobility cost analysed above.

No index exists yet, and none is needed at the scale `DESIGN.md` targets —
hundreds of tiles, tens to low hundreds of a population. This entry exists so
that when a decide-step or system scan looks like a wart, the fix is extending
the index behind the query surface, not reopening the entity model.
