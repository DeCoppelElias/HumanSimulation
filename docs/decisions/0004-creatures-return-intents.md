# 0004. Creatures return intents, the world resolves them

## Status

Accepted, 2026-09-03.

## Context

Today a creature changes the world itself. It computes a move and calls move.
That is the cheaper design: no resolver, no intermediate value, less machinery,
and a creature's whole behaviour readable in one class.

The cost is also visible in the current code. Legality rules live inside the
agent, which is why movement rerolls up to ten times to stay in bounds and then
gives up, and why every future rule about where you may step would land in every
creature type. Creatures act in sequence, so one acting early changes what
another sees in the same day.

## Decision

A brain receives a perception and returns one intent. The world applies it.

```java
public sealed interface Intent {
    record Move(Direction direction, int distance) implements Intent {}
    record Breed() implements Intent {}
    record Idle() implements Intent {}
}

public interface Brain extends Component {
    default Class<? extends Component> key() { return Brain.class; }
    Intent decide(Perception perception, RandomGenerator random);
}
```

A move carries a direction and a distance rather than a delta, matching the
existing step distribution over distances one to three and making the walked path
unambiguous. The resolver walks it one tile at a time and stops where the world
says it must.

`Intent` is sealed, so a switch with no default branch fails to compile when a
case is missing. `Brain` defaults its own key, so an implementation supplies only
the decision.

A perception carries the tiles within view range by straight-line distance, what
stands on each, and the components those things carry. A brain checks for
component presence by class reference (`tile.has(Edible.class)`), so a typo is a
compile error rather than a silent miss. Components that carry state worth
reading expose it through a read-only view (`tile.get(Edible.class).amount()`).
Adding a perceivable property is adding a component, which is already the pattern
for adding any capability. The perception API provides helpers for common queries
such as finding the nearest tile that carries a given component.

## Consequences

Terrain becomes cheap. Water blocking movement is one branch in the resolver
rather than knowledge every creature type carries.

Conflicts become visible, because every intent exists before any is applied.

Breeding is a decision rather than a system, reversing what an earlier draft of
this entry said. It passes the brain test on all three counts, and the argument
that had kept it a system depended on a faithful port that
[0002](0002-replace-the-model-layer-in-place.md) abandoned. It costs a creature
its move for that day, which is the tradeoff that makes it worth deciding, and it
allows behaviour a system could not express, such as holding off while the tile
is crowded.

Movement behaviour changes. A blocked creature walks as far as it legally can,
where today it rerolls and often stands still. Seeded runs will not match the old
ones step for step.
