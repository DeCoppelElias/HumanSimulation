# Founder benchmark

Status: idea.

## What it does

Scores one genome by founding a population with it. A few creatures with that
genome start a world with mutation off, on a fixed set of seeds, and the score
is how often their line survives and how large it grows. The set of worlds is a
preset, and grows with the world: plain grass, grass that withers fast, and
later a dry world or one with fire.

For example, the rabbit baseline keeps its line alive on 14 of 20 seeds. Scoring
genomes exported on days 0, 250, 500 and 1000 of an evolving run gives a
learning curve: one that keeps rising has room left, one that flattens at day
250 has met its ceiling.

## Why it is interesting

It compares brains. Two brains with different genes can only be compared by
what they achieve, and a founder test measures that in worlds the brains did not
evolve in, the way evolutionary robotics tests a controller across conditions.
A network brain that evolves to 19 of 20 where the scoring brain reaches 16
found something the fixed rules cannot express.

The learning curve answers the open question in
[network brain](network-brain.md) of when the fixed rules have run out: when the
scoring brain's curve flattens across seeds.

## What it would touch

Spawning from an exported genome, which is the
[brain library](brain-library.md). A preset per set of worlds in
`tools/experiments/`, and [comparison statistics](comparison-statistics.md)
for reading the scores. Whether a network brain's warm start matches the
scoring brain is checked by handing both the same perceptions, per
[0012](../decisions/0012-tests-target-brains-without-a-world.md), not by this
benchmark.

## Open questions

How many founders a test starts with, since one creature that dies early fails
the seed for reasons the genome had no part in.

Whether the score is survival, peak population, or both.

Which worlds the first preset holds.
