# Ancestor competition

Status: built, [0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).

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
rabbit descends from, and no lineage tag is needed. A script of its own,
`tools/ancestor_competition.py`, that borrows `tools/compare.py`'s runner and
reports counts of seeds rather than the
[comparison statistics](comparison-statistics.md), per
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).

## Open questions

Roadmap entry 9 settles one world, group size and ratio for its gate: the
world the genomes evolved in with more food, so the original genome can live
alone, ten of each group, one to one. Whether the result
depends on the starting ratio is still open.

Roadmap entry 12 settles one way to measure a lesson that a second species
teaches: rabbits evolved with co-evolving wolves against rabbits evolved without
them, tested beside four wolves drawn from the evolving arm's last day. Both
groups meet the same wolves, so the comparison between them stays fair, though
the evolving arm meets the lineage it learned against. Wolves at their baseline
were tried first, as a fixed opponent that sidesteps the Red Queen problem
(Cliff and Miller), where both species improving can look like neither
improving. Both rabbit arms evolve to about twice the baseline wolf's speed, so
those wolves starved within weeks and the test became a contest for grass, which
the control won. Measuring the wolves themselves still needs
day-1000 wolves against day-0 rabbits, and freezing one species while the other
evolves needs mutation per species rather than per world. With mutation on, a
group can no longer be read off an exact genome and needs a tag.
