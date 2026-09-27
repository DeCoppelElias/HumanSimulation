# Decision log

One entry per topic: title, status, context, decision, consequences. When the
choice on a topic changes, its entry is rewritten to state the current decision,
and `git log` keeps what it said before. A new entry is only for a new topic.
See [0001](0001-documentation-splits-into-four-artifacts.md).

`DESIGN.md` states what was decided. These say why, what was rejected, what it
costs, and what the concrete type shapes are.

- [0001](0001-documentation-splits-into-four-artifacts.md) Documentation splits
  into four artifacts: a design file, this log, an idea list, and a work plan.
- [0002](0002-replace-the-model-layer-in-place.md) Replace the model layer in
  place, growing from zero, one watchable package at a time.
- [0003](0003-entities-carry-components.md) Entities carry components, species
  are the recipe. The decision the rest hangs on.
- [0004](0004-creatures-return-intents.md) Creatures return one intent a day and
  the world resolves it. The resolver owns legality, and the dead leave at once.
- [0005](0005-a-day-is-an-ordered-list-of-systems.md) A day is an ordered list
  of systems, in six steps.
- [0006](0006-genomes-are-a-named-layout-of-gene-shapes.md) Genomes are a named
  layout of two gene shapes, and a child's reserve comes from its parent.
- [0007](0007-state-leaves-as-a-snapshot-commands-go-in.md) State leaves as a
  whole-world snapshot, and commands go in, draining at the start of a day.
- [0008](0008-brains-decide-systems-apply.md) Brains decide, systems apply. The
  test is whose rule it is.
- [0009](0009-terrain-is-entities-plus-tile-fields.md) Terrain is entities plus
  tile fields, split on whether a thing can be created and destroyed.
- [0010](0010-runs-replay-exactly-from-a-seed.md) Runs replay exactly from a
  seed: one generator, id ordering, decide before apply.
- [0011](0011-traversal-goes-through-queries.md) Traversal goes through queries
  that return fresh lists, which keeps large worlds reachable later.
- [0012](0012-tests-target-brains-without-a-world.md) Tests target brains
  without a world, and a headless runner watches for lost selection.
- [0013](0013-perception-is-one-type.md) Perception is one type, the builder is
  the seam, and components declare who sees each value.
- [0014](0014-resolve-order-comes-from-a-speed-gene.md) Resolve order comes from
  a speed gene, with equal speeds drawn at random.
- [0015](0015-geometry-lives-in-the-grid.md) Geometry lives in the grid: four
  directions for now, and the shape stays changeable.
- [0016](0016-feeding-takes-the-tile.md) Feeding takes the tile, and there is no
  contest.
- [0017](0017-every-number-is-a-setting-or-a-gene.md) Every number is a setting
  or a gene, and a setting is declared with a default.
