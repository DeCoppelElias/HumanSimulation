# 0018. Brain computation model and warm starting

## Status

Accepted, 2026-09-05. Revised 2026-09-27.

## Context

The `Brain` interface is settled in [0004](0004-creatures-return-intents.md): it
receives a `Perception` and returns one `Intent`. How it maps one to the other
is not.

Three computation models were considered.

Fixed rules with evolvable parameters. The brain has a rule structure written in
Java, such as approach the nearest edible, flee the nearest threat, else walk at
random, and the genome tunes only the parameters inside those rules: thresholds,
weights, distances. This is what the 2022 `MovementBehaviour` does.

A decision tree encoded in the genome. The genome holds the tree itself, and
mutations add, remove or change branches. The gene shapes are continuous and a
tree is discrete, so adding a branch is a structural jump rather than a nudge
along a smooth surface. Mutations can produce contradictory or invalid trees,
and the fitness landscape is jagged. Ruled out.

A neural network with weight genes. The perception is flattened to a vector,
multiplied through weight matrices stored in the genome, and a softmax produces
intent probabilities. Nothing is hardcoded, food-seeking included. The genome
needs a third shape for this, since network weights are not a normalised
distribution and may be negative.

One problem cuts across all three. A species whose first generation starts from
random values spends many generations rediscovering basic movement, and a
population that wanders at random for hundreds of days is not watchable.

## Decision

Two brain implementations, in order.

The fixed-rules brain comes first. Its rule structure is hardcoded and its
genome holds the parameters, so behaviour works from the first generation.

A neural network brain comes second, when the fixed-rules brain's ceiling is
hit. It starts from weights that approximate the fixed-rules brain, so evolution
begins from useful behaviour. The weight gene shape arrives with it, per
[0006](0006-genomes-are-a-named-layout-of-gene-shapes.md).

Warm starting lives in the species. A species carries a baseline genome
alongside its gene layout, per [0003](0003-entities-carry-components.md), and
every first-generation member starts from that baseline with mutation applied
rather than from random values. This holds for any brain.

## Consequences

Writing a species takes two things: a layout that declares which genes exist and
their bounds, and a baseline that gives them sensible starting values.

The fixed-rules brain's rule set is itself a design decision, made when that
brain is built. It defines what behaviour the simulation starts with and what
the network brain inherits when it takes over.

Flattening a variable-length perception into a fixed-size network input is open
until the network brain is built. Per-tile weight sharing with pooling, a
recurrent pass over tiles and attention are all candidates. A rough count for a
plain flattened network at this scale, tens of creatures each seeing tens of
tiles, comes out several orders of magnitude below what a CPU can do, so none
is ruled out on performance.
