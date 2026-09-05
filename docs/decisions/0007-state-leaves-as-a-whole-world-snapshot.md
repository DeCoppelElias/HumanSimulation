# 0007. State leaves the core as a whole-world snapshot

## Status

Accepted, 2026-09-03.

## Context

The interface currently reaches into the model for whatever it needs, and one
simulation rule has escaped upward as a result. Food regeneration lives in the
manager's automatic advance, whose only caller is the interface controller, so a
headless run of the world spawns no food at all. That bug is the argument for a
boundary on its own.

Three shapes were considered for how state crosses it. A snapshot lets a client
render from one message. Events make it replay history to know the current state.
Dirty regions make it ask.

## Decision

The core holds no reference to Swing, and no simulation rule lives above the
core. Each day the world produces an immutable snapshot of everything: the day
number, the grid dimensions, and for each tile its scalar fields and the entities
standing on it with their species, sprite key, tags and displayable values.

```java
public record WorldSnapshot(int day, int width, int height, List<TileView> tiles) {
    public record TileView(GridPosition at, Map<String, Double> fields, List<EntityView> entities) {}
    public record EntityView(
            int id, String species, String spriteKey,
            Set<String> tags, Map<String, Double> info) {}
}
```

Commands are the only way in. Spawning entities, resetting the world, editing a
species' settings, resetting the statistics and setting an entity alight are all
commands. The interface never calls a model method that is not one.

## Consequences

A headless run behaves exactly like a watched one, so a scripted run is possible
and the food regeneration bug cannot recur.

At a few hundred tiles a snapshot is a few thousand small records per day.

The cost is real if the world grows to tens of thousands of tiles, at which point
dirty regions become the answer. Adding a method that returns changes since a
given version is additive, and no existing caller breaks, because the interface
never reaches into the model directly.

Every command is a type someone has to write, where reaching into the model was
free. The current interface performs around twenty operations, so that is twenty
things to name before the switchover.

Two of them do not survive translation. Asking whether an entity is a human
becomes asking whether it has a brain, and asking a human for its view range
becomes reading a gene.
