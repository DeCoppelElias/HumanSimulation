# Mutation shadow

Status: idea.

## What it does

Makes genomes that changed by mutation alone, as many steps from the founder as
an evolved lineage, and runs them against the evolved genomes in an
[ancestor competition](ancestor-competition.md). If the evolved group wins,
selection did the work rather than mutation wandering somewhere useful.

For example, a lineage is 25 generations from its founder on day 1000. Applying
the mutation step 25 times to the founder, with no world and no selection, gives
the shadow. Evolved rabbits that beat the ancestors and also beat the shadow
show selection working. Evolved rabbits that tie with the shadow show that any
change helped and selection added nothing measurable.

## Why it is interesting

A start near a gene's bound makes a random walk look like learning. Entry 7's
`selection-gate` preset starts `grassPull` at 0.2 with a floor of 0, so
mutation alone pushes it up on average. Comparing against a neutral shadow is how artificial life separates
adaptation from drift: Bedau's neutral shadow models and the random-selection
runs in Yaeger's Polyworld both keep everything except selection.

It matters less once evolution starts from a good genome, such as a species'
baseline or a warm-started network brain.

## What it would touch

A generation count on each creature, so an export says how far a lineage has
come. A small entry point that applies the real mutation step repeatedly to a
genome, so the shadow uses the world's mutation code rather than a copy of it.

## Open questions

Whether one shadow per lineage is enough, or a lineage needs several, since one
random walk is a noisy reference.

Whether lineages in one world, with different generation counts, each get a
shadow of their own depth.
