# 0014. Resolve order comes from a speed gene

## Status

Accepted, 2026-09-27. Revised 2026-10-15.

## Context

[0010](0010-runs-replay-exactly-from-a-seed.md) replaces every incidental
ordering in the model with ascending id. That is right for storage, for
iteration and for what a tile returns, and it is wrong for the steps where order
changes outcomes.

Resolving in id order means the lower id acts first and reaches contested ground
first, every day, forever. An id records when a thing happened to spawn and
correlates with nothing else, so the advantage is permanent, invisible and
chosen by nobody. It is the same defect as the hash enumeration tiebreak in the
2022 code, one layer up.

Three alternatives were considered. Ordering by the intent, so a creature that
declared a three-tile move acts before one that declared one tile, needs no new
gene and rewards declaring a long move to buy initiative, and it gives breeding
and idling no natural place in the order. Shuffling the whole order each day
with the world's generator removes the bias and replaces it with noise, which
nothing can select on. It is the right answer for equal speeds alone. Breaking
ties on a second gene moves the tie one level down, couples two traits that have
nothing to do with each other, and still needs a tiebreak underneath.

A free advantage is not a tradeoff. Speed with no cost pins to the upper bound
of its range within a few dozen generations and becomes a constant.

The gene charges could be taken every day, apart from the eating interval. A
trait would then cost the same per day whatever the interval, but a creature's
days left would combine two rhythms.

## Decision

A `speed` scalar gene in a species' gene layout. The resolve step orders the
entities holding an intent by speed descending, and equal speeds are drawn at
random. An entity whose species has no speed gene resolves after those that do,
and ties there are drawn the same way.

The draw is reproducible because the id ordering feeds it. Take the entities
holding an intent in ascending id order, shuffle with the world's generator,
then stable sort by speed descending.

One order is computed per day and both the resolve step and the feed step use
it, per [0016](0016-feeding-takes-the-tile.md). Every other system iterates in
ascending id order.

Speed is charged for by the metabolism, in proportion to the gene, so it
trades against the reserve that feeds a creature's children. The rabbit's
`speed` runs from 0 to 1, with a baseline of 0.5 and a mutation size of 0.05,
since only the ranking matters and the charge sets the scale.

View range has the same defect, since seeing farther never hurts a brain that
walks to the nearest grass, per
[0023](0023-rabbits-score-the-tiles-they-can-reach.md). It is charged the same
way, per tile of range, by a species setting. It is charged on the rounded
range, the tiles actually seen, so a fraction that buys nothing costs nothing.
Both charges fold into the eating charge, so each charge on the eating interval
is `eatingCharge + speed * speedCharge + round(viewRange) * viewCharge`, and a
creature's days left stays one formula. By default `speedCharge` is 0.1 and
`viewCharge` 0.03, so a baseline rabbit pays 1.14 a day.

## Consequences

Initiative becomes a trait under selection and the first real tension between
two genes, since a fast creature pays for its head start in food.

Whether the charge is right is an empirical question rather than a design one,
and the headless runner from [0012](0012-tests-target-brains-without-a-world.md)
answers it. A speed mean that pins to the bound within a few dozen generations
means the charge is too small.

A reserve charge is a placeholder for both. Every gene needs a tradeoff, and the
wanted kind is one specific to the trait, such as far sight blurring what is
close. `docs/ideas/gene-tradeoffs.md` holds that, and a gene that gets its own
tradeoff loses its charge.

Coupling speed to how far a creature may move is the other available cost and
stays available. Taking both at once would make a failed experiment unreadable.

Replay is untouched, since the shuffle reads the world's one generator over a
list already in id order.

A change to one creature's speed reshuffles the resolve order and therefore the
draw order from the world's one generator, so two runs whose commands differ in
a way that changes a single speed diverge completely. That is already true of
any change to behaviour.

The cost is a shuffle and a sort per day over the entities holding an intent,
and a handful of draws that shift the rest of the day's random stream.

How fast the interface plays days is the playback rate, and has nothing to do
with this gene. The interface never calls it speed, because two unrelated speeds
in one project is a bug waiting for a reader in a hurry.
