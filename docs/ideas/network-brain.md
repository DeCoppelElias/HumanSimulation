# Network brain

Status: idea.

## What it does

A brain whose decision is a small neural network, with its weights as genes,
started from weights that reproduce the fixed-rules brain. Selection
then refines it from working behaviour rather than from random wandering.

## Why it is interesting

It is the second brain
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md) plans,
for when the fixed rules run out, and the first thing a brain library would
hold. A network can find behaviour nobody wrote as a rule, which is what the
vision in `DESIGN.md` is about.

## What it would touch

A second gene shape for weights, per
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md). A way to
flatten a perception into a fixed-size input, which 0018 leaves open. The
fixed-rules brain of
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md) is already
a weighted sum of features followed by a softmax, so a network whose first
layer computes those features starts from its genes directly. Anything the
network adds beyond that starts at weights that change nothing.

## Open questions

How a variable-length perception becomes a fixed input: per-tile weight sharing
with pooling, a recurrent pass over tiles, or attention.

Whether the network's input is the scoring brain's features or something rawer,
which would need a fitting step to imitate the rules.

What "the fixed rules have run out" means, so it is clear when to start.
