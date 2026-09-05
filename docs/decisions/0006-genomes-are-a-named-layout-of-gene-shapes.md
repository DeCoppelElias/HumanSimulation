# 0006. Genomes are a named layout of gene shapes

## Status

Accepted, 2026-09-03.

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
or shrinking by one, then renormalising, which is what the current step variation
does.

Reproduction stays asexual. A child spawns at the parent's position with the
parent's genome mutated and a reserve of zero.

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
simplex: they are not normalised and may be negative. A third shape —
`WeightArray` — is needed when the neural net brain is added, but is deferred
until then, per [0013](0013-brain-computation-model-and-warm-starting.md).

The species carries a baseline genome alongside the layout. First-generation
members start from that baseline with mutation applied, rather than from random
values. This is how warm starting is expressed: the species author sets the
baseline to encode sensible starting behaviour.

A gene nothing reads does nothing, so a new trait still needs a brain or a system
that consults it.

The mutation rate is a constant in the layout rather than a gene, so it cannot
itself come under selection. Making it one is a later change and needs no new
shape.
