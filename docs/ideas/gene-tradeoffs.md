# Gene tradeoffs

Status: idea.

## What it does

Gives each gene that would otherwise only help a cost of its own kind, specific
to what the trait is, instead of a charge against the reserve. Seeing far could
make a creature easier to be seen, or blur what it sees up close. Speed could
cost precision, or make a creature overshoot.

It is also how gait could evolve again. 2022 evolved how far a creature walks,
as a distribution over one to three tiles. Since
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md) a rabbit
chooses its distance up to a reach that is a setting, the same for every rabbit.
As a gene, reach would pin to its bound unless something prices it.

## Why it is interesting

A gene with no cost pins to its upper bound and becomes a constant, per
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md). A reserve
charge fixes that, and it fixes it the same way for every gene, so every trait
ends up traded against the same currency. Tradeoffs that differ by trait give
populations more distinct ways to be good, and more for a watcher to notice.

## What it would touch

Whatever the trait touches: the `Sense` for view range, the resolver for speed
and reach.
The charges in the metabolise step would shrink or go.

## Open questions

Which trait gets an original tradeoff first. View range is the obvious one,
since its charge is a placeholder.

Whether a tradeoff that adds randomness to a perception keeps brain tests exact,
since a brain is tested by handing it a perception.
