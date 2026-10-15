# 0004. Creatures return intents, the world resolves them

## Status

Accepted, 2026-09-03. Revised 2026-10-15.

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

The 2022 code let a human breed only on days that were exact multiples of its
age. A creature that chooses to breed would then have to choose it on that one
day, and one short of reserve that day waited a whole interval. Breeding was
also one cost with the child's share a fraction of it. A parent whose reserve
just covered the cost was left with less than a day's charge and died that
evening unless it ate, and three numbers in three units, a cost, a fraction and
a margin, were harder to read on a panel than three amounts.

Dead entities could stay in the world until the clean-up step, marked dead. The
body would then occupy its tile for the rest of the day and could be perceived,
but perception happens only in the decide step, before anything can die, so
nobody would ever see it. What remains is a dead-but-present state that every
later system has to remember to skip.

A brain also has to know what it may choose between, such as which directions
exist, and later whom it could attack or whether it can breed today. Three
homes were considered for that. The perception could carry it, but a sense
builds the perception, per [0013](0013-perception-is-one-type.md), and a new
sense would then have to remember to copy in the grid's directions. The brain
could be handed it at birth through its `Spawn`, which carries a genome and a
species and not the grid, and every brain test would pass it at construction
too. Or a brain could infer directions from the neighbouring tiles it
perceives, which needs no new type, and since a tile off the grid is absent, a
creature at an edge could never choose to walk into it, moving the edge rule
from the resolver into the brain.

What a brain is offered first took the shape of one field per kind of action:
the landings, a tile to end the day on plus the intent that walks there, with
breeding and attack targets to join as fields of their own. Every new action
then changed the shape of `Options`. Offering breeding as one more landing at
the creature's own tile needed no new field, but a landing stopped meaning a
tile you walk to, and a brain that scored only tiles would breed as often as it
stayed, by accident.

Offering every action and letting the resolver turn an illegal pick into an
idle day was simpler for the options. Machine learning calls the other way
action masking, and the comparison in Huang and Ontañón, *A Closer Look at
Invalid Action Masking in Policy Gradient Algorithms* (2020), found that
penalising illegal picks stops working as they grow more common, where masking
keeps working. Selection is the learner here, and a wasted day is that penalty.

Legality could live in each component, which would answer what its action
offers and whether it may happen. Breeding alone already reads two components,
the window and the reserve, and a move reads the grid and whoever blocks it, so
a component deciding legality would reach into others and into the world. A
generic `legal(entity, intent)` engine, or rules registered behind one
interface, was the other end. A registry looks a rule up by intent class
instead of switching over the sealed `Intent`, which loses the compile error
that makes every new action get a rule.

## Decision

A brain receives a perception and the day's options, and returns one intent. The world applies it.

```java
public sealed interface Intent {
    record Move(Direction direction, int distance) implements Intent {}
    record Breed() implements Intent {}
    record Idle() implements Intent {}
}

public interface Brain extends Component {
    default Class<? extends Component> key() { return Brain.class; }
    Intent decide(Perception perception, Options options, RandomGenerator random);
}

public sealed interface Option {
    Intent intent();
    record Move(Direction direction, int distance, int dx, int dy) implements Option {}
    record Breed() implements Option {}
    record Idle() implements Option {}
}

public record Options(List<Direction> directions, List<Option> offered) {}
```

An option is something a creature may choose today, and an intent is what it
chose. `Option` mirrors `Intent` case for case, and each option returns the
intent of the same name, so adding an action adds a case of the same name to
both. A `Move` option also carries the offset it lands on, worked out by the
grid, because an intent's direction and distance become a tile only through the
grid, and a brain never measures, per [0015](0015-geometry-lives-in-the-grid.md).

`Options` is what the world offers a creature when the day starts, built from
the grid and the creature's own components, and kept apart from what it
senses. A new sense never touches it, and a new capability never touches a
sense. It holds the grid's directions in a fixed order and one ordered list of
options: idling, then each direction one tile at a time up to the species'
`stride` setting, stopping at the edge, then breeding. A species with no
`stride` is offered no moves. The order is fixed, so a pick replays and a
network brain reads the list as a fixed encoding of its outputs. An attack
joins as a case when it is built, and the shape of `Options` stays the same.

Only legal options are offered. An option offered at dawn is still not a
promise: the resolver decides what happens, and a move can be blocked by
something that moved first.

A move's distance is at least one, checked when it is built, per
[0022](0022-warnings-fail-the-build-and-nothing-is-null.md). A move carries a
direction and a distance rather than a delta, matching the tiles a brain scores
per [0023](0023-rabbits-score-the-tiles-they-can-reach.md) and making the
walked path unambiguous. `Intent` and `Option` are sealed, so a switch with no
default branch fails to compile when a case is missing. `Brain` defaults its own key and offers no views, since a brain shows
only in what it does, so an implementation supplies only the decision. The
perception's shape is [0013](0013-perception-is-one-type.md).

The resolve step owns legality.

Each action has one rule in the world, such as `MoveRule` and `BreedRule`, with
two methods: `options` says what the action offers a creature today, and
`resolve` applies the chosen intent. Each takes what it reads, such as the
creature, its position, the grid or the day, and holds no state, since the world
rebuilds its grid on a reset. The offer and the outcome live in one class, so
they cannot drift apart, and they stay two checks, since the resolver runs after
faster creatures have acted. The world's
`options(entity)` calls each rule in the fixed order, and the resolve switch
hands each intent to its rule, so those two places list every action. Idling
needs no rule, since it is always allowed and changes nothing. A component
answers only about its own state, such as whether its window is open, and a rule
that combines components or reads the world belongs to the world.

It applies intents in the day's order from
[0014](0014-resolve-order-comes-from-a-speed-gene.md), over a list of ids fixed
at the start. An entity spawned during the step is not on the list, so a newborn
first acts, and first feeds, on the day after its birth.

A tile holds any number of entities. Movement is blocked only by an entity
carrying a blocking component, so crowding is felt through food running out
rather than through space. The resolver walks a move one tile at a time, and a
blocked mover walks as far as it legally can and stops there.

An entity that dies leaves the world at once, whichever step killed it. When a
dead entity's turn arrives its id no longer resolves and it is skipped. Remains,
if they are ever wanted, are spawned by whatever does the killing.

Breeding reads four species settings, per
[0017](0017-every-number-is-a-setting-or-a-gene.md), all in reserve but the
first: `breedInterval`, `childReserve`, `breedingLoss` and `parentMargin`. A
`Fertility` component holds the day of the creature's birth or its last breed.
Breeding is legal once `breedInterval` days have passed since then, and stays
legal until it breeds, while the reserve is at least `childReserve +
breedingLoss + parentMargin`. The parent pays `childReserve + breedingLoss`, so
it keeps at least `parentMargin`. The child spawns on the parent's tile with a
mutated genome and `childReserve` as its reserve, per
[0006](0006-genomes-are-a-named-layout-of-gene-shapes.md), and `breedingLoss` is
gone.

There is no attack. Nothing in the base simulation needs one, and designing it
before there is a predator to ask would be guesswork. When the predator on the
roadmap needs violence, it arrives as a new intent, never as something the feed
step does to a creature.

## Consequences

Terrain becomes cheap. Water blocking movement is one branch in the resolver
rather than knowledge every creature type carries.

Conflicts become visible, because every intent exists before any is applied.

One intent per creature per day is the real constraint. A choice that only
becomes available partway through a day cannot be a decision, because the
decisions are already collected by then, so anything that wants to be chosen has
to be visible when the day starts. That is why violence, when it arrives, is an
intent rather than a rule the feed step applies.

Breeding costs a creature its move for that day, which is the tradeoff that
makes it worth deciding, and it allows behaviour a schedule could not express,
such as holding off while the tile is crowded.

Movement differs from 2022. A blocked creature walks as far as it legally can,
where the 2022 code rerolls and often stands still, so seeded runs do not match
the old ones step for step.

Many creatures on one tile is normal, so the interface has to show a stack
rather than one sprite. Roadmap entry 3 draws one with a count, and anything
richer is `docs/ideas/drawing-a-stacked-tile.md`. A
child is always born somewhere, since a tile cannot be full.

Nothing dead acts, so once creatures can kill each other, being early in the
order matters in a fight as well as at food.

Removal at death means every loop that can kill has to walk a copy of the ids,
which [0011](0011-traversal-goes-through-queries.md) makes the rule for all
traversal. A rule that wants to react to a death has to be run by whatever
kills, since no body is left to find later in the day.

A day can contain fewer actions than it collected intents, so anything counting
actions has to read what was applied rather than what was decided.

When a new intent joins the sealed type, the switch in the resolver fails to
compile until that case is handled, which is the reminder that nothing else
provides. A new action costs an `Intent` case, an `Option` case of the same
name, its rule, and a line in the world's `options` and in its resolve switch,
and every brain that switches over `Option` has to decide what to do with it.

A brain never spends a day on an action that cannot happen, so selection weighs
only real choices. What the world rules out, a creature cannot learn to avoid,
such as breeding itself thin. `parentMargin` could become a gene if caution
itself should evolve.
