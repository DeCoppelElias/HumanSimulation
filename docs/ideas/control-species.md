# Control species

Status: idea.

## What it does

Runs a species that never learns beside the rabbits. It uses the scoring brain
of [0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md) with
`grassPull`, `hungerDrive` and `rabbitPull` fixed at zero and every gene's
mutation size at zero, so it picks among its moves evenly and stays that way
across generations.

## Why it is interesting

It shows whether evolution beats chance. If the rabbits outlive and outbreed a
species that walks at random on the same grass, selection is doing something.
It needs no new brain, since a scoring brain with nothing to pull it is a random
walk, and the deleted `RandomBrain` stays deleted.

## What it would touch

A new species beside `Rabbit`, with its own name and sprite. It only means
something once breeding and mutation exist, from roadmap entry 6.

## Open questions

Whether it eats the same grass as the rabbits, so the two compete, or a food of
its own, so each measures against the same supply without interfering.

Whether the census and the chart mark it as a control, or it is one more line.

Whether it is still needed beside an
[ancestor competition](ancestor-competition.md) and a
[mutation shadow](mutation-shadow.md), which ask what selection adds without a
second species that rabbits do not count as rabbits.

What its `breedPull` is fixed at, since breeding is a choice its brain scores
and the control has to breed at a rate that means something.
