# 0010. Runs replay exactly from a seed

## Status

Accepted, 2026-09-03.

## Context

Emergence and breakage look identical from outside. The only thing that separates
enjoying a surprise from fearing it is being able to watch it again, so
reproducibility is a property of the vision.

The world already owns one seeded generator that every behaviour draws from, and
`DeterminismTest` guards that a seeded run replays exactly. That has to survive
the rebuild.

Two sources of incidental ordering exist today. Action order comes from insertion
order in a list, which is stable but chosen by nobody. And `GridTile.collectFood`
enumerates a `Hashtable` to pick the food with the fewest contenders, so when
several foods tie, which one a creature collects falls out of hash enumeration
order. Which creature wins a contested food is already a stated rule using the
seeded generator, so that half is sound today.

## Decision

The world owns one random generator, passed to everything that draws from it. No
class constructs its own.

Entities are held sorted by id, every system iterates in ascending id order, and
asking the grid what stands on a tile returns occupants in ascending id order.

Deciding finishes before anything is applied.

## Consequences

The hash-order tiebreak in `collectFood` is replaced by a stated one. Which food
a creature takes when several tie becomes a gene the feed system consults, the
same shape as aggression, rather than a hidden property of a hash table.

A run replays exactly from a seed, so a strange outcome can be reproduced, and a
headless run can be compared against itself across changes.

Sorted iteration is a cost paid on every query. A tile's occupants come back
sorted, so the grid either keeps them sorted on insert or sorts on read, and any
index or spatial structure added later has to preserve id order in its results.

The guarantee forecloses running any step in parallel. Nothing needs that today,
and if a large world ever did, the guarantee is what would have to give.
