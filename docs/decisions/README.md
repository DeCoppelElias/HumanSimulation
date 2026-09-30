# Decision log

One entry per topic: title, status, context, decision, consequences. When the
choice on a topic changes, its entry is rewritten to state the current decision,
and `git log` keeps what it said before. A new entry is only for a new topic.
See [0001](0001-documentation-splits-into-five-artifacts.md).

`DESIGN.md` states what was decided. These say why, what was rejected, what it
costs, and what the concrete type shapes are.

- [0001](0001-documentation-splits-into-five-artifacts.md) Documentation splits
  into five artifacts: a design file, this log, an idea list, a roadmap and a
  work plan.
- [0002](0002-replace-the-model-layer-in-place.md) Replace the model layer in
  place, growing from zero, one watchable package at a time.
- [0003](0003-entities-carry-components.md) Entities carry components, species
  are the recipe, and DDD supplies the vocabulary. The decision the rest hangs
  on.
- [0004](0004-creatures-return-intents.md) Creatures return one intent a day and
  the world resolves it. The resolver owns legality, and the dead leave at once.
- [0005](0005-a-day-is-one-method.md) A day is one method in six
  steps, passing intents as local data, with world processes as a list.
- [0006](0006-genomes-are-a-named-layout-of-gene-shapes.md) Genomes are a named
  layout of two gene shapes, and a child's reserve comes from its parent.
- [0007](0007-state-leaves-as-a-snapshot-commands-go-in.md) State leaves as a
  whole-world snapshot, and commands go in, draining at the start of a day. The
  core is a hexagonal domain, wired by hand, and a `jdeps` test holds the line.
- [0008](0008-brains-decide-systems-apply.md) Brains decide, systems apply. The
  test is whose rule it is.
- [0009](0009-terrain-is-entities-plus-tile-fields.md) Terrain is entities plus
  tile fields, split on whether a thing can be created and destroyed.
- [0010](0010-runs-replay-exactly-from-a-seed.md) Runs replay exactly from a
  seed: one generator, id ordering, decide before apply.
- [0011](0011-traversal-goes-through-queries.md) Traversal goes through queries
  that return fresh lists, and an index behind them rather than a split entity
  type keeps large worlds reachable later.
- [0012](0012-tests-target-brains-without-a-world.md) Tests come first and
  target brains without a world, and a headless runner watches for lost
  selection.
- [0013](0013-perception-is-one-type.md) Perception is one type, the builder is
  the seam, brains read components by class, and each component declares what
  others see of it.
- [0014](0014-resolve-order-comes-from-a-speed-gene.md) Resolve order comes from
  a speed gene, with equal speeds drawn at random.
- [0015](0015-geometry-lives-in-the-grid.md) Geometry lives in the grid: four
  directions for now, and the shape stays changeable.
- [0016](0016-feeding-takes-the-tile.md) Feeding takes the tile, and there is no
  contest.
- [0017](0017-every-number-is-a-setting-or-a-gene.md) Every number is a setting
  or a gene, and a setting is declared with a default.
- [0018](0018-brain-computation-model-and-warm-starting.md) A fixed-rules brain
  first, a network brain later, and every species starts from a baseline
  genome.
- [0019](0019-workflows-are-skills-and-invariants-are-scripts.md) Workflows
  are skills, invariants are scripts, and promoting an idea or changing an
  accepted decision waits for Elias.
