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

A start near a gene's bound makes a random walk look like learning, since a
step toward the bound is cut there and a step away never is. Comparing against
a neutral shadow is how artificial life separates adaptation from drift:
Bedau's neutral shadow models and the random-selection runs in Yaeger's
Polyworld both keep everything except selection.

It matters less once evolution starts mid-range. Entry 9 made `grassPull` and
`hungerDrive` signed, per
[0023](../decisions/0023-creatures-score-the-tiles-they-can-reach.md), so its
poor starting genome has no floor to climb off and its gate needs no shadow. A
start near a bound, such as `whim` near its floor or a gene pinned by a later
species, would need one again.

## What it would touch

A generation count on each creature, so an export says how far a lineage has
come. A small entry point that applies the real mutation step repeatedly to a
genome, so the shadow uses the world's mutation code rather than a copy of it.

## Open questions

Whether one shadow per lineage is enough, or a lineage needs several, since one
random walk is a noisy reference.

Whether lineages in one world, with different generation counts, each get a
shadow of their own depth.
