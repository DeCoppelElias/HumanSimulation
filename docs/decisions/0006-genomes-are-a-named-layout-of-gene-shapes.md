# 0006. Genomes are a named layout of gene shapes

## Status

Accepted, 2026-09-03. Revised 2026-09-27.

## Context

Today the genome is constructor parameters. View range threads through three
constructors, each behaviour class implements its own variation method, and
adding a trait means editing signatures.

The genome holds more than scalars. The step amount is a normalised distribution
over step distances whose length itself mutates between one and three, alongside
plain numbers like view range.

A record of typed fields per species was the alternative. It gives compile-time
safety and a typo does not compile. The cost is that generic mutation stops
existing: every species needs its own mutation method, or one written by
reflection over record components, which trades the safety back for a runtime
failure in a less obvious place. A variable-length distribution is also awkward
in a record, since the bounds on its length have nowhere to live.

A child also needs a starting reserve. A reserve of zero kills it on its first
metabolism charge unless it has eaten, and it cannot eat on its birth day, per
[0004](0004-creatures-return-intents.md), so nearly every child would starve. A
grace period of some days without charges was the other way out. It adds an age
check to the metabolise step, and it gives every child days of life that no food
paid for.

## Decision

A species declares a layout of gene specifications. A genome holds values for
that layout and can produce a mutated copy. The entity owns its genome, per
[0003](0003-entities-carry-components.md).

```java
public sealed interface GeneSpec {
    record Scalar(String key, double min, double max, double mutationSize) implements GeneSpec {}
    record Simplex(String key, int minLength, int maxLength, double mutationSize) implements GeneSpec {}
}
```

A scalar mutates by an offset within its mutation size, clamped to its bounds. A
simplex mutates by shifting weight between two entries and occasionally growing
or shrinking by one, then renormalising, which is what the current step
variation does.

Reproduction stays asexual. A child spawns at the parent's position with the
parent's genome mutated. Its starting reserve is its endowment, a fraction of
the breeding cost the parent pays, set by a species setting per
[0017](0017-every-number-is-a-setting-or-a-gene.md). Metabolism charges a
newborn like everyone else.

Scalars are continuous. Traits that are conceptually whole numbers, such as view
range, round at read time, so selection still sees differences that a stored
integer would flatten away.

## Consequences

Adding an evolvable trait is one entry in a species layout. No constructor
signature changes anywhere, because mutation, inheritance and clamping are
implemented once per gene shape.

Keys are strings, so a typo is a runtime failure where a field would have been a
compile error. Reading an unknown key throws, which keeps the failure loud and
local.

The two shapes cover the current genome exactly. Network weights are not a
simplex, since they are not normalised and may be negative, so a third shape
for them arrives with the network brain in
[0018](0018-brain-computation-model-and-warm-starting.md). A brain whose
topology evolves would need the genome to carry a graph, which is a fourth shape
and real work.

The species carries a baseline genome alongside the layout, and first-generation
members start from it with mutation applied, per
[0018](0018-brain-computation-model-and-warm-starting.md).

A gene nothing reads does nothing, so a new trait still needs a brain or a
system that consults it.

The mutation size is declared in the layout rather than being a gene, so it
cannot itself come under selection. Making it one is a later change and needs no
new shape.

Energy passes from parent to child, so every day a child lives was paid for by
food someone gathered. A parent's condition shows in how often it can breed,
since its reserve has to cover the cost, rather than in what each child gets.
The endowment fraction is a setting, and making it a gene later would let
selection choose between few well-fed children and many thin ones, with no new
shape.
