# Network brain

Status: idea.

## What it does

A brain whose decision is a small neural network, with its weights as genes,
started from weights fitted once to imitate the fixed-rules brain. Selection
then refines it from working behaviour rather than from random wandering.

## Why it is interesting

It is the second brain
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md) plans,
for when the fixed rules run out, and the first thing a brain library would
hold. A network can find behaviour nobody wrote as a rule, which is what the
vision in `DESIGN.md` is about.

## What it would touch

A third gene shape for weights, per
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md). A way to
flatten a perception into a fixed-size input, which 0018 leaves open. A one-time
fitting step that records the fixed-rules brain's choices and fits the weights
to them, run offline and not in a world.

## Open questions

How a variable-length perception becomes a fixed input: per-tile weight sharing
with pooling, a recurrent pass over tiles, or attention.

Where the fitting step lives, and whether its output is committed as a baseline
genome.

What "the fixed rules have run out" means, so it is clear when to start.
