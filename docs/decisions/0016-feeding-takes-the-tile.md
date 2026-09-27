# 0016. Feeding takes the tile, and there is no contest

## Status

Accepted, 2026-09-27.

## Context

In the 2022 model, eating was the only moment two creatures ever met. There was
no attack, and no creature could perceive another, so conflict had to be bolted
onto food collection: stand on the same pile, roll aggression, and one of you
dies. The rules were that one aggressive contender takes the food, several means
the seeded generator picks a winner and the rest die, and none means the
peaceful ones split it evenly.

The rebuild removes the constraint that produced that design. A brain sees its
neighbours and can return an attack, so violence has a home where a creature
chooses it, conditional on what it sees and on how hungry it is.

Keeping the contest as well means two combat systems, one decided and one
applied. The applied one would be a behaviour that belongs to the individual by
the test in [0008](0008-brains-decide-systems-apply.md) and still runs as a
system, because the contest does not exist until every intent has been
collected. Aggression could then never be conditional.

## Decision

The feed step walks the day's order from
[0014](0014-resolve-order-comes-from-a-speed-gene.md), the same order the
resolve step used.

A creature takes every edible on its tile that its diet accepts. The value is
credited to its metabolism and the edible is removed, so a creature later in the
order finds nothing there. There is no contest, no aggression gene, and no
splitting. Which edible is taken first does not matter, since all of them are.

Only entities in the day's order feed, and the order holds the entities that
returned an intent, so a feeder is something with a brain. A child born during
the resolve step is not in the day's order, so it does not feed on its birth
day.

## Consequences

Every behaviour that differs between individuals is decided by a brain.

Speed matters twice, in acting and in eating, and the second one is where it
mostly earns its cost.

The 2022 regression rule that a food fight leaves exactly one winner has nothing
left to guard, per [0012](0012-tests-target-brains-without-a-world.md). The
contest is written down in `docs/ideas/aggression-at-contested-food.md` so
nobody restores it thinking it went missing.

A newborn missing its first feed costs nothing. It is born on its parent's tile,
and the parent has already taken every edible there.

Early runs select on finding food and on getting there first, and on nothing
else, which is thinner than what the 2022 simulation shows a watcher.

A species with a metabolism and no brain would starve, which
[0003](0003-entities-carry-components.md) already notes nothing prevents.

Feeding is unconditional, so a creature eats a pile it does not need. A limit is
`docs/ideas/stomach-capacity.md`, and if it arrives, how much to eat becomes a
question a brain could be asked.
