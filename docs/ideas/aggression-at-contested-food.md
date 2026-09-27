# Aggression at contested food

Status: idea.

## What it does

Several creatures stand on one pile of food and settle it between them. The 2022
rules were: one aggressive contender takes the food, several means the seeded
generator picks a winner and the rest die, and none means the peaceful ones
split it evenly. Aggression was a per-individual number that passed to children.

## Why it is interesting

It is the most visible emergent thing the 2022 simulation does. Aggressive and
peaceful strains coexist, and which one is winning changes as the food supply
changes. It is also the only source of death by another creature, so without it
early runs select on finding food and nothing else.

## What it would touch

The feed step, which currently has no contest at all. A gene in the human
species layout. Nothing else, since it needs no new component and no new intent.

## Open questions

Whether it belongs anywhere, since
[0016](../decisions/0016-feeding-takes-the-tile.md) settles feeding without it.

Whether it is still wanted once the `Attack` intent exists, which gives violence
a home where a creature chooses it. Keeping both means two combat systems, one
decided and one automatic.

Whether the automatic version can be made conditional, which
[0008](../decisions/0008-brains-decide-systems-apply.md) says it cannot, since
the contest does not exist until every intent has been collected.
