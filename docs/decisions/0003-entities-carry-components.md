# 0003. Entities carry components, species are the recipe

## Status

Accepted, 2026-09-03.

## Context

Hunger, flammability, edibility and blocking cut across any hierarchy of creature
classes, so where that state lives is the decision the rest hangs on.

Capability as an interface on a class tree fails on state rather than dispatch.
Fuel and burn progress have to live somewhere, Java interfaces hold no fields,
and default methods cannot reach them. Every implementing class redeclares the
same fields, or holds a delegate and forwards to it, which is a component system
built by accident.

A species descriptor carrying capability flags was the closest alternative. It
handles new creature types, terrain, new traits and swappable brains as cheaply
as components do. It charges its cost once per new kind of capability, being a
field on the descriptor plus a system, and it needs a second flag for any state
that comes and goes.

A full entity component system, with components in global tables keyed by id, was
also considered. Its benefit is cache locality across thousands of entities,
which buys nothing measurable at this size, and it costs debuggability and type
safety.

## Decision

An entity is an id, a position, a species, a genome and a map from component type
to component. A species is the recipe for a kind of entity.

```java
public interface Component {
    Class<? extends Component> key();
}

public record Spawn(Genome genome, Map<String, Double> settings) {}

public record Species(
        String name,
        String spriteKey,
        List<GeneSpec> geneLayout,
        Map<String, Double> settings,
        Optional<Function<Spawn, Brain>> brain,
        List<Function<Spawn, Component>> parts) {}
```

A component declares the type it is filed under, so a query for `Brain.class`
finds a `UtilityBrain`. The brain has its own field rather than sitting among the
parts, because it is the seam that gets swapped, and it is optional because
terrain is a species too and water decides nothing.

At spawn the world builds a `Spawn` from the genome it rolled or inherited plus
the species settings, and hands it to the brain and every part.

This entity replaces the existing `Entity`, `GridContent`, `Human` and `Food`
hierarchy. Rendering reads the sprite key instead of matching class names against
literal strings, and the parameters panel edits a species' settings map instead
of a flat table of world parameters.

## Consequences

Fire settles the choice against species flags, and only because being combustible
and being alight are different things. Being flammable comes from the recipe and
is permanent. Being on fire is attached on ignition and detached when it burns
out. There is no burning flag to keep consistent on everything that might ever
burn, and no way to hold burn progress without being alight.

This buys no query speed. The world keeps no component index, so finding
everything on fire is a scan with a filter, exactly as the flag version would be.
If profiling ever makes that matter, an index is internal to the world.

The cost is two concepts instead of one, a rule for telling them apart, and
losing the compiler's guarantee that a creature has a brain. Nothing stops a
species being built with a metabolism and no way to eat.

Adding a new kind of capability costs one component and one system. Inventing new
kinds of capability over years is the point of the project.
