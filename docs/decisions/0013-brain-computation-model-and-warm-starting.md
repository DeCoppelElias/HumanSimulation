# 0013. Brain computation model and warm starting

## Status

Accepted, 2026-09-05.

## Context

The `Brain` interface is settled ([0004](0004-creatures-return-intents.md)): it
receives a `Perception` and returns one `Intent`. How it maps perception to intent
internally is not specified there.

Three computation models were considered.

**Fixed rules, evolvable parameters.** The brain has a hardcoded rule structure
— approach nearest `Edible`, flee nearest threatening component, else random walk
— and the genome only tunes parameters within those rules: thresholds, weights,
distances. The rules are written in Java; evolution can only tune how aggressively
each rule fires. This is what the current `MovementBehaviour` already does.

**Decision tree encoded in the genome.** The genome encodes the tree structure
itself — conditions, branches, and actions. Mutations add, remove, or modify
branches. The genome shapes (`Scalar`, `Simplex`) are continuous, but tree
structure is discrete: adding a branch is a structural jump, not a nudge along a
smooth surface. Mutations can produce contradictory or invalid trees, and the
fitness landscape is jagged. Ruled out.

**Neural network with weight-array genes.** The perception is flattened to a
vector, multiplied through weight matrices stored in the genome, and a softmax
produces intent probabilities. Everything including food-seeking is in the weights;
nothing is hardcoded. The genome would need a third shape — `WeightArray` — since
network weights are not normalised distributions and may be negative.

A related problem cuts across all options: a species whose first generation starts
from random values spends an unacceptable number of generations rediscovering basic
movement. The simulation is meant to be watchable; a population that moves randomly
for hundreds of days is not.

## Decision

Two brain implementations, in order.

**First: fixed-rules brain.** Hardcoded rule structure, genome encodes parameters.
This gives working behaviour from generation one. The rules define what the
simulation can observe at the start.

**Second: neural net brain, when the fixed-rules ceiling is hit.** Initialised
from weights that approximate the fixed-rules brain, so evolution starts from
already-useful behaviour rather than from random. The `WeightArray` gene shape is
introduced at this point, not before.

Warm starting is expressed through the species definition. The species carries a
**baseline genome** alongside its gene layout. All first-generation members start
from that baseline with mutation applied, rather than from random values. This
applies to any brain type: the species author writes a baseline genome that encodes
sensible starting behaviour.

## Consequences

The species definition gains a baseline genome field alongside the gene layout.
Writing a new species requires both: a layout that declares which genes exist and
their bounds, and a baseline that gives those genes reasonable starting values.

The `WeightArray` gene shape is deferred until the neural net brain is added.
[0006](0006-genomes-are-a-named-layout-of-gene-shapes.md) is updated to reflect
that `Simplex` does not cover network weights.

The fixed-rules brain's rule set is itself a design decision, made when that brain
is implemented. Choosing those rules carefully matters: they define what behaviour
the simulation starts with and what the neural net brain inherits when it takes
over.

Flattening a variable-length perception (view range varies per creature) into a
fixed-size network input is an open question for when the neural net brain is
built; options include per-tile weight sharing with pooling, a recurrent pass over
tiles, or attention, and no choice is made yet. A rough flop count for a plain
flattened-input network at this simulation's scale (20 creatures, tens of visible
tiles each, deciding twice a second) comes out several orders of magnitude below
what a CPU can do, so none of these options is ruled out on performance.
