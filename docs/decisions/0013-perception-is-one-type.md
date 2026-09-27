# 0013. Perception is one type, the builder is the seam

## Status

Accepted, 2026-09-27.

## Context

[0004](0004-creatures-return-intents.md) gives the brain, the intent and the
resolver a shape. The perception is the input half of the seam the whole design
hangs a swap on, and per [0012](0012-tests-target-brains-without-a-world.md) it
is also the test surface: a brain is tested by handing it a perception, with no
world at all.

Experimenting with what a creature can sense is wanted. The question is where
the variation lives.

A perception type per species or per sense was the first alternative. It makes a
brain generic in its input, so every brain declares which perception it takes
and the test suite becomes a matrix of brains against perceptions. The variation
it buys is available more cheaply by keeping one type and varying what fills it.

A summarised perception was the second: the direction of the nearest food, the
number of neighbours, the distance to the edge. It is far nicer to write a brain
against, and it fixes in advance what is allowed to matter. That is the opposite
of the vision, where a wolf avoids a burning tile without anyone having
introduced fire to wolves. Adding a perceivable property would mean editing the
summary and every brain reading it.

String tags and string-keyed values were the third. Adding a perceivable
property is free, a typo is a silent miss at runtime, and every tag needs some
component to be responsible for it. Components already say what a thing is, so a
brain can ask for them directly.

Whether a creature sees its own state matters to
[0004](0004-creatures-return-intents.md), since a brain that cannot read its own
reserve can only breed on a schedule, which is a system's job.

Whether it sees the state of others is the other half. Making all of it visible
leaks exact inner state: a predator would read how close each prey animal is to
starving, which no animal can know.

## Decision

One type, filled by a per-species builder. A brain reads components by class.

```java
public record Perception(SelfView self, List<TileView> tiles) {
    public record SelfView(int id, Parts parts) {}
    public record TileView(int dx, int dy, Map<String, Double> fields, List<EntityView> entities) {}
    public record EntityView(int id, String species, Parts parts) {}
}

public interface Parts {
    boolean has(Class<? extends Component> type);
    <V extends Record> Optional<V> view(Class<V> viewType);
}
```

Every component declares what its carrier sees of it and what creatures in range
see of it, as read-only view records, and neither has a default. `Brain` is the
one exception, per [0004](0004-creatures-return-intents.md).

```java
// on Component
Record ownView();
Optional<Record> seenView();   // empty: invisible to others
```

`Metabolism` shows its reserve to its carrier and nothing to others, so a
neighbour cannot tell it is there. `Edible` shows its amount to everyone. A
burning component shows its heat to everyone. A brain asks
`entity.parts().has(Edible.class)` or
`self.parts().view(Metabolism.Own.class)`, and a misspelt class does not
compile.

`self` carries the own view of every component the perceiver has. An
`EntityView` carries only the components that offer a seen view. Neither carries
genes, because a brain built from its `Spawn` already closed over its own.

Positions are relative to the perceiver, and no absolute position appears
anywhere in a perception. A tile outside the grid is absent from the list, which
is how a creature perceives an edge. Tile fields stay string-keyed, since they
belong to the grid rather than to a component.

Ordering is fixed: tiles by straight-line distance ascending, then by `dy` and
`dx`; entities within a tile by ascending id. A brain that takes the first
edible tile it finds makes the same choice on a replay.

The type carries derived helpers, such as `nearest(Edible.class)` and
`tilesWith(Edible.class)`. They are pure functions over the same data, so they
cost nothing in flexibility.

A `Sense` component builds the perception, through queries per
[0011](0011-traversal-goes-through-queries.md). The decide step asks the
entity's `Sense` and falls back to a circular view by the `viewRange` gene when
there is none. Giving a wolf smell means a new `Sense` in its species parts, and
no brain, no test and no other species changes.

## Consequences

Breeding is a real decision, so a creature can hold off while starving.

Adding a perceivable property is adding a component or a field to a view, which
is already how any capability is added.

A brain sees something only if a component offers a view of it, so what one
creature can know about another is a choice every component makes explicitly. It
is also readable from a species' parts without running anything, so a test can
assert it.

A sense that sees more than the seen view, such as a keen eye that can tell who
is weak, would arrive as a new `Sense` with access to the own views of others,
so that knowledge can become a trait under selection rather than a fixed fact of
the world.

A fixed-rules brain names the component classes it reacts to. A network brain
needs a generic encoding of whatever components it meets, which is part of the
open input question in
[0018](0018-brain-computation-model-and-warm-starting.md).

No absolute position means a brain cannot navigate to a fixed point on the map,
and a homing behaviour would arrive as a new `Sense` rather than as coordinates.

A perception is allocated per creature per day, about thirty small records at
view range three. If that ever matters it is internal to the builder.

A perception and the whole-world snapshot from
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md) are the same idea at
two ranges and stay two types. The snapshot has no perceiver and carries a
sprite key for drawing, which has no business in a decision input. The
duplicated shape is the price of that separation.
