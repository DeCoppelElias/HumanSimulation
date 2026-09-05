# Decision log

One entry per settled choice: title, status, context, decision, consequences.
Entries are append-only. A choice that is reversed gets a new entry, and the old
one has its status changed to say which entry supersedes it.

`DESIGN.md` states what was decided. These say why, what was rejected, what it
costs, and what the concrete type shapes are.

- [0001](0001-documentation-splits-into-three-artifacts.md) Documentation splits
  into three artifacts: a design file, this log, and a work plan.
- [0002](0002-replace-the-model-layer-in-place.md) Replace the model layer in
  place, growing from zero, one watchable package at a time.
- [0003](0003-entities-carry-components.md) Entities carry components, species
  are the recipe. The decision the rest hangs on.
- [0004](0004-creatures-return-intents.md) Creatures return one intent a day and
  the world resolves it.
- [0005](0005-a-day-is-an-ordered-list-of-systems.md) A day is an ordered list of
  systems, in six steps.
- [0006](0006-genomes-are-a-named-layout-of-gene-shapes.md) Genomes are a named
  layout of two gene shapes.
- [0007](0007-state-leaves-as-a-whole-world-snapshot.md) State leaves the core as
  a whole-world snapshot, and commands are the only way in.
- [0008](0008-brains-decide-systems-apply.md) Brains decide, systems apply. The
  test is whose rule it is.
- [0009](0009-terrain-is-entities-plus-tile-fields.md) Terrain is entities plus
  tile fields, split on whether a thing can be created and destroyed.
- [0010](0010-runs-replay-exactly-from-a-seed.md) Runs replay exactly from a
  seed: one generator, id ordering, decide before apply.
- [0011](0011-traversal-goes-through-queries.md) Traversal goes through queries,
  which keeps large worlds reachable later.
- [0012](0012-tests-target-brains-without-a-world.md) Tests target brains without
  a world, and a headless runner watches for lost selection.
- [0013](0013-brain-computation-model-and-warm-starting.md) Fixed-rules brain
  first, neural net second; species carries a baseline genome for warm starting.
