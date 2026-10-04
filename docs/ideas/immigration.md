# Immigration

Status: idea.

## What it does

A world process that spawns creatures of a species every few days, the way food
arrives on an interval. A world starts empty, and its starting population is
spawn commands an adapter sends on day 0, so without this a population that
dies out stays dead.

## Why it is interesting

It keeps a run watchable past an extinction, and it would let a species arrive
partway through a run, such as wolves appearing once rabbits are established.

## What it would touch

One world process with world settings for the species, the count and the
interval, beside the food spawning from roadmap entry 5.

## Open questions

Immigrants carry the species' baseline genome, so every arrival dilutes what
selection has built. That works against the selection gate in roadmap entry 7,
so immigration would need to be off by default, or immigrants would need a
genome drawn from somewhere other than the baseline.
