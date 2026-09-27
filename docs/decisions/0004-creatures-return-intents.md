# 0004. Creatures return intents, the world resolves them

## Status

Accepted, 2026-09-03. Revised 2026-09-27.

## Context

In the 2022 model a creature changes the world itself. It computes a move and
calls move. That is the cheaper design: no resolver, no intermediate value, less
machinery, and a creature's whole behaviour readable in one class.

The cost is also visible in that code. Legality rules live inside the agent,
which is why movement rerolls up to ten times to stay in bounds and then gives
up, and why every future rule about where you may step would land in every
creature type. Creatures act in sequence, so one acting early changes what
another sees in the same day.

Moving legality out of the creature only helps if the rules have one home, so
what counts as legal is part of this decision rather than left to whoever writes
the first system.

Breeding as a system that fires on a schedule was the alternative to breeding as
an intent. Breeding passes the brain test in
[0008](0008-brains-decide-systems-apply.md) on all three counts, so it is an
intent.

Dead entities could stay in the world until the clean-up step, marked dead. The
body would then occupy its tile for the rest of the day and could be perceived,
but perception happens only in the decide step, before anything can die, so
nobody would ever see it. What remains is a dead-but-present state that every
later system has to remember to skip.

## Decision

A brain receives a perception and returns one intent. The world applies it.

```java
public sealed interface Intent {
    record Move(Direction direction, int distance) implements Intent {}
    record Breed() implements Intent {}
    record Idle() implements Intent {}
}

public interface Brain extends Component {
    default Class<? extends Component> key() { return Brain.class; }
    Intent decide(Perception perception, RandomGenerator random);
}
```

A move carries a direction and a distance rather than a delta, matching the step
distribution over distances one to three and making the walked path unambiguous.
`Intent` is sealed, so a switch with no default branch fails to compile when a
case is missing. `Brain` defaults its own key, so an implementation supplies
only the decision. The perception's shape is
[0013](0013-perception-is-one-type.md).

The resolve step owns legality.

It applies intents in the day's order from
[0014](0014-resolve-order-comes-from-a-speed-gene.md), over a list of ids fixed
at the start. An entity spawned during the step is not on the list, so a newborn
first acts, and first feeds, on the day after its birth.

A tile holds any number of entities. Movement is blocked only by an entity
carrying a blocking component, so crowding is felt through food running out
rather than through space. The resolver walks a move one tile at a time, and a
blocked mover walks as far as it legally can and stops there.

An entity that dies leaves the world at once, whichever step killed it. When a
dead entity's turn arrives its id no longer resolves and it is skipped, and an
attack naming a target that is gone resolves as nothing. Remains, if they are
ever wanted, are spawned by whatever does the killing.

Breeding takes its interval, its cost and the child's endowment from species
settings, per [0017](0017-every-number-is-a-setting-or-a-gene.md). A parent
breeds when the interval has elapsed and its reserve covers the cost. The child
spawns on the parent's tile with a mutated genome and a reserve funded from the
cost, per [0006](0006-genomes-are-a-named-layout-of-gene-shapes.md).

Attacking arrives with the combat slice and not before, as a fourth case,
`Attack(int targetId)`. Two things about it are settled: how far a creature can
strike is a component, so reach belongs to the creature rather than to the
resolver, and an attack is a contest both sides can lose. What it rolls against,
whether reach is a gene or a setting, and what a kill yields are left open until
there is a predator to ask.

## Consequences

Terrain becomes cheap. Water blocking movement is one branch in the resolver
rather than knowledge every creature type carries.

Conflicts become visible, because every intent exists before any is applied.

One intent per creature per day is the real constraint. A choice that only
becomes available partway through a day cannot be a decision, because the
decisions are already collected by then, so anything that wants to be chosen has
to be visible when the day starts. That is why violence is an `Attack` intent
rather than something the feed step does to a creature.

Breeding costs a creature its move for that day, which is the tradeoff that
makes it worth deciding, and it allows behaviour a schedule could not express,
such as holding off while the tile is crowded.

Movement differs from 2022. A blocked creature walks as far as it legally can,
where the 2022 code rerolls and often stands still, so seeded runs do not match
the old ones step for step.

Many creatures on one tile is normal, so the interface has to show a stack where
it currently draws one sprite. That is `docs/ideas/drawing-a-stacked-tile.md`. A
child is always born somewhere, since a tile cannot be full.

Nothing dead acts, which makes being early a real advantage and gives the speed
gene another thing to buy once combat exists.

Removal at death means every loop that can kill has to walk a copy of the ids,
which [0011](0011-traversal-goes-through-queries.md) makes the rule for all
traversal. A rule that wants to react to a death has to be run by whatever
kills, since no body is left to find later in the day.

A day can contain fewer actions than it collected intents, so anything counting
actions has to read what was applied rather than what was decided.

When `Attack` joins the sealed intent type, the switch in the resolver fails to
compile until that case is handled, which is the reminder that nothing else
provides.
