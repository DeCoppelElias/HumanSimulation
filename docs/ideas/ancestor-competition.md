# Ancestor competition

Status: planned, roadmap entry 9.

## What it does

Tests whether evolution produced something better by letting the world judge.
Genomes alive at the end of an evolving run go into a fresh world with mutation
off, beside the genomes the run started from, and the run reports which group
takes over. A second arm runs each group alone in its own world, on the same
seeds, and compares their populations.

For example, rabbits from day 1000 of seed 7 and the poor genome they started
from, five each, on a new seed. On day 300 eleven evolved rabbits are alive and
two ancestors, so the evolved group won. Alone, the evolved world holds twelve
rabbits and the ancestral one eight, so the win did not come from a race that
shrinks the population.

## Why it is interesting

It names no gene. A strategy nobody expected passes as long as it lives better,
which keeps the surprise `DESIGN.md` wants while still checking that evolution
works. It is how Lenski's long-term E. coli experiment measures fitness: evolved
and ancestral strains compete in one flask.

The shared world alone rewards anything that beats a neighbour, including
speed and view raised at a cost the whole population pays. The arm with each
group alone catches that: a group that wins head to head but holds a smaller
population alone is better at fighting, not at living.

Entry 7's `mutation-ramp` preset compares worlds where everything mutates
against worlds where nothing does, so turning mutation off removes variation
and the cost of bad mutations at once. Here mutation is off in both groups, and only where their
genomes came from differs.

## What it would touch

Exporting the genomes alive on a given day and spawning from a file of them,
which is the [brain library](brain-library.md). With mutation off a child is an
exact copy, so a genome exported at the end of the test says which group a
rabbit descends from, and no lineage tag is needed. A preset and a report mode
in `tools/compare.py` for group shares, with
[comparison statistics](comparison-statistics.md).

## Open questions

Which world the test runs in: the one the genomes evolved in, its harshest
conditions, or a fixed set, and whether a ramp belongs in it.

How many genomes each group starts with, and whether the result depends on the
starting ratio.

Once a second species evolves, freezing one while the other evolves needs
mutation per species rather than per world, and comparing them needs day-1000
wolves against day-0 rabbits, since both improving can look like neither
improving (Cliff and Miller's Red Queen problem). With mutation on, a group can
no longer be read off an exact genome and needs a tag.
