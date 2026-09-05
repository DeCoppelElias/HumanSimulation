# 0008. Brains decide, systems apply

## Status

Accepted, 2026-09-03.

## Context

Once everything that occupies a tile is an entity, no type remains to tell a wolf
from a fire. Something has to say which things are asked for a decision and which
are acted upon, or the question gets answered case by case.

An earlier form of this rule tested whether a thing could have done otherwise.
That test collapses, because it accidentally means stochastic, and randomness
sits on both sides. Fire rolls for spread. A weighted-random brain is also a dice
roll and is unambiguously a brain. Half the test was doing no work.

## Decision

The test is whose rule it is. A system's procedure belongs to the world and
applies identically to everything of its kind. A brain's procedure belongs to the
individual, varies from its neighbour's, and passes to its children.
`DESIGN.md` carries the full statement and the guidance that follows from it.

Neither randomness nor per-individual numbers settle it. Metabolism subtracts a
cost that a gene could set and is still one procedure applied to everyone.

## Consequences

The rule is a diagnostic rather than a fence. Give fire a genome whose spread
probability mutates, and let spreading count as reproduction, and it has become a
brain by this definition, correctly, because there are then fire lineages under
selection.

Nothing enforces it. Someone can give water a brain and the decide step will
start asking it for intents, and no test will fail. The rule works only as long
as it is consulted.

Breeding is modelled as a decision, per [0004](0004-creatures-return-intents.md),
and passes the brain test on all three counts.
