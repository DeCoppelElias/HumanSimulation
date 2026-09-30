# 0003. Entities carry components, species are the recipe

## Status

Accepted, 2026-09-03. Revised 2026-09-30.

## Context

Hunger, flammability, edibility and blocking cut across any hierarchy of
creature classes, so where that state lives is the decision the rest hangs on.

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

A full entity component system, with components in global tables keyed by id,
was also considered. Its benefit is cache locality across thousands of entities,
which buys nothing measurable at this size, and it costs debuggability and type
safety.

The model follows domain-driven design, and DDD's usual shape puts behaviour on
a class per kind, a `Rabbit` that knows how to eat. That is the class hierarchy
this entry rejects, so DDD is taken for its vocabulary and its tactical patterns
and not for rich per-kind objects. Its strategic layer was also considered:
bounded contexts, repositories and domain events. This is one simulation with
one context and nothing persisted, so each would be structure with nothing to
organise.

## Decision

An entity is an id, a position, a species, a genome and a map from component
type to component. A species is the recipe for a kind of entity.

```java
public interface Component {
    Class<? extends Component> key();
}

public record Spawn(Genome genome, Species species) {}

public record Species(
        String name,
        String spriteKey,
        List<GeneSpec> geneLayout,
        Genome baseline,
        List<Setting> settings,
        Optional<Function<Spawn, Brain>> brain,
        List<Function<Spawn, Component>> parts) {}
```

`Setting` is [0017](0017-every-number-is-a-setting-or-a-gene.md), and the
baseline genome is [0018](0018-brain-computation-model-and-warm-starting.md). A
component also declares what its carrier and its neighbours can perceive of it,
per [0013](0013-perception-is-one-type.md).

A component declares the type it is filed under, so a query for `Brain.class`
finds a `FixedRulesBrain`. The brain has its own field rather than sitting among
the parts, because it is the seam that gets swapped, and it is optional because
terrain is a species too and water decides nothing.

At spawn the world builds a `Spawn` from the genome it drew from the baseline or
inherited from a parent, plus the species itself, whose settings are read live,
and hands it to the brain and every part.

In DDD terms the world is the aggregate root, and nothing inside it changes
except through it. An entity is a DDD entity, identified by its id alone.
`GridPosition`, `Genome`, `Intent`, `Perception` and the snapshot records are
immutable value objects. The rules of a day are domain services on the world
rather than methods on a creature. The words in `DESIGN.md` are the ubiquitous
language: the code uses them, and a new concept gets its word there before it
gets a class.

This entity replaces the existing `Entity`, `GridContent`, `Human` and `Food`
hierarchy. Rendering reads the sprite key instead of matching class names
against literal strings, and the parameters panel is generated from each
species' declared settings instead of a flat table of world parameters.

## Consequences

Fire settles the choice against species flags, and only because being
combustible and being alight are different things. Being flammable comes from
the recipe and is permanent. Being on fire is attached on ignition and detached
when it burns out. There is no burning flag to keep consistent on everything
that might ever burn, and no way to hold burn progress without being alight.

This buys no query speed. The world keeps no component index, so finding
everything on fire is a scan with a filter, exactly as the flag version would
be. If profiling ever makes that matter, an index is internal to the world.

The cost is two concepts instead of one, a rule for telling them apart, and
losing the compiler's guarantee that a creature has a brain. Nothing stops a
species being built with a metabolism and no way to eat.

A reader expecting DDD finds creatures without behaviour. The behaviour is in
the day's steps, and a component holds state and says what can be seen of it.

Adding a new kind of capability costs one component, plus either a new system or
a branch in an existing one. Inventing new kinds of capability over years is the
point of the project.
