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

## Decision

No system iterates the entity collection or the grid directly. Every traversal
goes through a query on the world or the grid.

A query returns a fresh list rather than a view of the world's collections. A
system can therefore remove or spawn entities while walking the result, which
removal at death in [0004](0004-creatures-return-intents.md) needs, and an id in
the list that no longer resolves when its turn comes is skipped.

## Consequences

An index, a spatial structure or a partial snapshot can be added later without a
caller changing.

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
