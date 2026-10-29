# Component interactions

Status: idea.

## What it does

The world holds a list of interactions, each naming an active component, a
passive one, a scope such as the same tile, and an effect. A step runs every
interaction that matches a pair of entities, so eating is one row, an eater
part meeting a food part, and the world never names food.

## Why it is interesting

A capability becomes a component plus a row, which is a stronger form of the
claim in `DESIGN.md` that a capability costs a component and a system or a
branch. Diet falls out of it: a species eats what its eater parts pair with,
and an omnivore carries two.

## What it would touch

Feeding, which becomes the first runner of the list, and
[0005](../decisions/0005-a-day-is-one-method.md), since what happens when two
things meet would live in a table rather than in the day method. Order becomes
data: which entity acts first, which row runs first when two match, and whether
a removed entity is skipped by later rows, all of which
[0010](../decisions/0010-runs-replay-exactly-from-a-seed.md) depends on.

An event system, where the world fires events that any part may handle, was
the broader alternative. It spreads behaviour over handlers with no one place
that shows the order, which is hard to keep deterministic and readable.

## Open questions

When to build it. Roadmap entry 10 left it out because the roadmap holds one
interaction, eating, and a list built for one case fits the second badly. Fire,
burning meeting flammable across neighbouring tiles, is the first interaction
of another shape and the point to reconsider.

Whether scope, order and effect are the right columns, which fire would show.
