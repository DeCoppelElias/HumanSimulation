# 0018. Feeding takes the tile, and there is no contest

## Status

Accepted, 2026-09-27.

## Context

In the 2022 model, eating was the only moment two creatures ever met. There was
no attack, and no creature could perceive another, so conflict had to be bolted
onto food collection: stand on the same pile, roll aggression, and one of you
dies. The rules were that one aggressive contender takes the food, several means
the seeded generator picks a winner and the rest die, and none means the peaceful
ones split it evenly.

The rebuild removes the constraint that produced that design. A brain sees its
neighbours and can return an attack, so violence has a home where a creature
chooses it, conditional on what it sees and on how hungry it is.

Keeping the contest as well means two combat systems, one decided and one
applied. The applied one is the single exception
[0008](0008-brains-decide-systems-apply.md) has to apologise for, since the
contest does not exist until every intent has been collected, which is why
aggression can never be conditional.

## Decision

One order is computed for a day, per
[0014](0014-resolve-order-comes-from-a-speed-gene.md), and both the resolve step
and the feed step use it.

A creature takes every edible on its tile that its diet accepts. The value is
credited to its metabolism and the edible is removed, so a creature later in the
order finds nothing there. There is no contest, no aggression gene, and no
splitting.

Which edible is taken first does not matter, since all of them are, so the
tiebreak gene that
[0010](0010-runs-replay-exactly-from-a-seed.md) promised is dropped.

Until the combat slice, hunger is the only thing that kills.

## Consequences

[0008](0008-brains-decide-systems-apply.md) loses its known exception. Every
decision is a decision.

Speed matters twice, in acting and in eating, and the second one is where it
mostly earns its cost.

Eight of the nine regression rules from
[0012](0012-tests-target-brains-without-a-world.md) carry over. The ninth, that a
food fight leaves exactly one winner, is retired on purpose, and the rule it
guarded is written down in `docs/ideas/aggression-at-contested-food.md` so nobody
restores it thinking it went missing.

Early runs select on finding food and on getting there first, and on nothing
else, which is thinner than what the 2022 simulation shows a watcher.

Feeding is unconditional, so a creature eats a pile it does not need. A limit is
`docs/ideas/stomach-capacity.md`, and if it arrives, how much to eat becomes a
question a brain could be asked.
