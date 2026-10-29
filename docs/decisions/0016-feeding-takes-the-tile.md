# 0016. Feeding takes the tile, and there is no contest

## Status

Accepted, 2026-09-27. Revised 2026-10-29.

## Context

In the 2022 model, eating was the only moment two creatures ever met. There was
no attack, and no creature could perceive another, so conflict had to be bolted
onto food collection: stand on the same pile, roll aggression, and one of you
dies. The rules were that one aggressive contender takes the food, several means
the seeded generator picks a winner and the rest die, and none means the
peaceful ones split it evenly.

The rebuild removes the constraint that produced that design. A brain sees its
neighbours, so when violence is wanted it can be an intent a creature chooses,
conditional on what it sees and on how hungry it is.

Keeping the contest as well means two combat systems, one decided and one
applied. The applied one would be a behaviour that belongs to the individual by
the test in [0008](0008-brains-decide-systems-apply.md) and still runs as a
system, because the contest does not exist until every intent has been
collected. Aggression could then never be conditional.

Every eater taking every edible held until a predator needed a diet, since a
wolf that eats grass is not a predator. A diet could name the species a
creature eats, which means editing every herbivore for each new plant and has
the wolf name the rabbit. It could be a kind field inside one `Edible`
component, but a brain reads components by class, per
[0013](0013-perception-is-one-type.md), so it would have to look inside a view
to tell grass from meat. It could be a set of accepted kinds on `Metabolism`,
which hides the diet inside a component about the reserve, or a separate
eater class per diet, which is a class to hold one different value. A list of
interactions the world runs, with eating as one row, is
`docs/ideas/component-interactions.md`, left out while eating is the only
interaction there is.

`Eater` showed nothing to others while nothing needed to know a diet. A rabbit
that should learn to keep away from wolves has to see something a wolf shows,
and a component that names a wolf as a threat, or a rule that marks whatever
eats what a rabbit carries as dangerous, would tell it the answer. Showing what
an eater takes leaves the reaction to its pull genes, per
[0023](0023-creatures-score-the-tiles-they-can-reach.md).

Feeding was first a step of its own after every move had resolved, which let a
wolf catch only a rabbit that stayed put, per
[0005](0005-a-day-is-one-method.md).

A predator could kill through an attack intent, per
[0004](0004-creatures-return-intents.md), and then eat. A wolf always wants to
eat, so for now there is nothing for a brain to decide, and killing goes
through feeding until something has to choose whether to kill.

## Decision

A creature eats at the end of its own turn in the resolve step, whatever it
chose, so feeding walks the day's order from
[0014](0014-resolve-order-comes-from-a-speed-gene.md), per
[0005](0005-a-day-is-one-method.md).

`Edible` is an interface, the amount a food is worth and what others see of
it. Each kind of food is a component of its own that implements it, with its
own key: `Plant`, which grass carries, and `Meat`, which a rabbit carries. Each
shows its amount to others, and its feature, per
[0013](0013-perception-is-one-type.md). What a food is worth is a setting of the
species that carries it: grass's `nourishment` is 15 and the rabbit's is 30. An
`Eater` component holds the food classes its carrier takes, fixed in the
species recipe rather than a setting or a gene. A rabbit takes `Plant`, and a
wolf takes `Meat`. `Eater` shows others what it takes, as one feature per food,
such as `takes.Meat`.

A creature with an `Eater` and a `Metabolism` takes every occupant of its tile
that carries a food class in its set. The value is credited to its metabolism
and the occupant is removed as eaten, so a creature later in the order finds
nothing there. Food can be a creature: a wolf on a rabbit's tile eats the
rabbit, and a rabbit eaten before its turn in the order neither moves nor
feeds. There is
no contest, no aggression gene, and no splitting. Which food is taken first
does not matter, since all of it is.

Only entities in the day's order feed, and the order holds the entities that
returned an intent, so a feeder is something with a brain. A child born during
the resolve step is not in the day's order, so it does not feed on its birth
day.

## Consequences

Every behaviour that differs between individuals is decided by a brain.

Speed decides who reaches food first, and for prey whether it is still there
when a predator arrives. A wolf whose turn comes before a rabbit's eats it on
the rabbit's own tile, and one whose turn comes after finds the tile empty, so
a wolf and a rabbit race on the same gene and both pay for it.

The 2022 regression rule that a food fight leaves exactly one winner has nothing
left to guard, per [0012](0012-tests-target-brains-without-a-world.md). The
contest is written down in `docs/ideas/aggression-at-contested-food.md` so
nobody restores it thinking it went missing.

A rabbit sees what a wolf takes and a wolf sees what a rabbit carries, so what
each does about the other is a pull gene that selection moves, and nothing in
the world says a wolf is dangerous.

A newborn missing its first feed costs nothing. It is born on its parent's tile,
and the parent has already taken every food there that it eats.

Predation is decided only by where each creature chooses to stand and by who
moves first. A rabbit
cannot fight back and a wolf cannot spare a rabbit it stands beside. When either
is wanted, the attack intent in 0004 comes back.

A new plant is eaten by every creature that takes `Plant`, with no change to
any of them.

Early runs select on finding food and on getting there first, and on nothing
else, which is thinner than what the 2022 simulation shows a watcher.

A species with a metabolism and no brain or no `Eater` would starve, which
[0003](0003-entities-carry-components.md) already notes nothing prevents.

Feeding is unconditional, so a creature eats a pile it does not need. A limit is
`docs/ideas/stomach-capacity.md`, and if it arrives, how much to eat becomes a
question a brain could be asked.

A diet cannot evolve while it is fixed in the recipe. `docs/ideas/diet.md` keeps
that question.
