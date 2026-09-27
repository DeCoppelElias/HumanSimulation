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

Whether a creature sees its own state matters to
[0004](0004-creatures-return-intents.md), since a brain that cannot read its own
reserve can only breed on a schedule, which is a system's job.

Whether it sees the state of others is the other half. Making every published
value visible to every neighbour is the simplest answer, and it leaks exact
inner state: a predator would read how close each prey animal is to starving,
which no animal can know. Keeping values private and letting components signal
publicly through tags, such as a `weak` tag below some threshold, was also
considered. Each threshold is then a setting that decides in advance what may
matter, the same fault as the summarised perception.

## Decision

One type, filled by a per-species builder.

```java
public record Perception(SelfView self, List<TileView> tiles) {
    public record SelfView(int id, Set<String> tags, Map<String, Double> values) {}
    public record TileView(int dx, int dy, Map<String, Double> fields, List<EntityView> entities) {}
    public record EntityView(int id, String species, Set<String> tags, Map<String, Double> values) {}
}
```

Positions are relative to the perceiver, and no absolute position appears
anywhere in a perception. A tile outside the grid is absent from the list, which
is how a creature perceives an edge.

Every component declares each value it publishes and who may see it. There is no
default audience.

```java
public enum Audience { SELF, NEARBY }

public record Observable(String key, Audience audience) {}

// on Component
List<Observable> observables();
Map<String, Double> publish();
```

Metabolism declares `reserve` as `SELF`, breeding declares its time until ready
as `SELF`, edibles declare `nutrition` as `NEARBY`, and a burning component
declares its heat as `NEARBY`. Publishing a key that was not declared throws.

`self` carries every value the perceiver's own components publish. An
`EntityView` carries only the `NEARBY` values of the entity it shows. Neither
carries genes, because a brain built from its `Spawn` already closed over its
own.

Ordering is fixed: tiles by straight-line distance ascending, then by `dy` and
`dx`; entities within a tile by ascending id. A brain that takes the first
edible tile it finds makes the same choice on a replay.

The type carries derived helpers, such as `nearest(tag)`, `tilesWithTag` and
`occupantsWithTag`. They are pure functions over the same data, so they cost
nothing in flexibility.

A `Sense` component builds the perception, through queries per
[0011](0011-traversal-goes-through-queries.md). The decide step asks the
entity's `Sense` and falls back to a circular view by the `viewRange` gene when
there is none. Giving a wolf smell means a new `Sense` in its species parts, and
no brain, no test and no other species changes.

## Consequences

Breeding is a real decision, so a creature can hold off while starving.

A brain sees a value only if some component publishes it and declares it
visible, so publishing is a design act the constructor enforces. A component
that publishes nothing is invisible to the creature carrying it.

What one creature can know about another is data, readable from a species' parts
without running anything, so a test can assert it and the interface can show it.

A sense that reads some `SELF` values of others, such as a keen eye that can
tell who is weak, would arrive as a new `Sense`, so that knowledge can become a
trait under selection rather than a fixed fact of the world.

No absolute position means a brain cannot navigate to a fixed point on the map,
and a homing behaviour would arrive as a new `Sense` rather than as coordinates.

A perception is allocated per creature per day, about thirty small records at
view range three. If that ever matters it is internal to the builder.

Tags and value keys are strings, so a typo is a runtime failure, the same trade
accepted for genes in [0006](0006-genomes-are-a-named-layout-of-gene-shapes.md)
and for settings in [0017](0017-every-number-is-a-setting-or-a-gene.md). An
undeclared value key at least fails where it is published.

A perception and the whole-world snapshot from
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md) are the same idea at
two ranges and stay two types. The snapshot has no perceiver and carries a
sprite key for drawing, which has no business in a decision input. The
duplicated shape is the price of that separation.
