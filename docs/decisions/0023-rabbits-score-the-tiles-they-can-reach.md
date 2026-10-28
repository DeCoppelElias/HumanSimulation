# 0023. Rabbits score the tiles they can reach

## Status

Accepted, 2026-10-12. Revised 2026-10-28.

## Context

[0018](0018-brain-computation-model-and-warm-starting.md) makes the first brain
a fixed set of rules whose parameters are genes, and leaves the rules themselves
to be decided when that brain is built. They define what the simulation starts
with and what a network brain inherits.

The 2022 rules were a lottery between strategies. Each day a human rolled
between wandering and walking to food, and when it walked to food it rolled
again between four ways of choosing one: the closest, a random visible one, the
one farthest from other humans, and the one nearest other food. It closed the x
gap before the y gap, and rolled how far to walk from a distribution over one to
three tiles. Its genes were the chances in those rolls.

Keeping that shape, as a scalar for wandering against food and a distribution
over the food choices, was the first alternative. A gene that shifts weight
between named strategies flips a creature between discrete behaviours, so
selection is hard to see working, and entry 9 is the gate where it has to be
seen. A brain has no memory, so a random choice made again each day drifts
between targets where 2022 kept walking to one. A new threat needs a new
strategy written for it. And a rolled distance walks past food one tile away on
most days.

Rolling the distance and scoring only the directions was the second. It kept
the step distribution as a gene, and it kept the overshoot, since the score
would be fighting the roll.

Within scoring, the distance to grass and the closeness of other rabbits could
each be measured two ways. Summed closeness to every visible grass rewards
clusters, and it flattens with range, so a rabbit drifts toward distant grass
rather than walking to it, and between two equal patches the best tile can be
the empty middle. Distance to the nearest rabbit, used for avoidance, keeps
growing with range, so a rabbit avoiding others is pushed to the edge of its
vision whatever the crowd, and two rabbits count as one.

Hunger could multiply the pull of grass rather than add to it. Then a rabbit
with no standing pull ignores food even when starving, and the strategy of
eating only when hungry cannot exist. A threshold gene saying where hunger
starts to matter was more expressive, and a third gene for a signal with little
to select on until breeding gives a reserve a use.

Turning scores into a pick by taking the best with some fixed chance of a
uniform pick was simpler to explain. It makes the second best tile no likelier
than the worst, so a careless rabbit wanders off instead of fumbling near the
food, and hunger loses its focusing effect.

Breeding could be chosen outside the softmax. A `breedMargin` gene, breeding
whenever the reserve left after paying covers that many days, gives selection
the clearest story, thin parents against safe ones, but it is a hard gate that a
small change in the gene flips all at once. Always breeding when offered needs no
gene and stops breeding being a decision, which undoes why
[0004](0004-creatures-return-intents.md) made it an intent.

`grassPull` and `hungerDrive` first had a floor of 0, since a pull away from
food is never good. A gate that starts rabbits from a genome that barely aims
for food, such as `grassPull` at 0.2 and `hungerDrive` at 0.5, starts them near
that floor. A gene that close to
its floor climbs by mutation alone, because a step down is cut at the bound and
a step up never is, so a rise could not be told from learning. A floor also
leaves the worst strategy, walking away from food, out of reach of selection.

## Decision

Each day a rabbit scores every tile it could land on and picks one.

The candidates are the options it is offered, per
[0004](0004-creatures-return-intents.md): idling, each of the grid's directions
at each distance from one up to `stride`, a species setting with a default of 3,
and breeding when it is legal. A move is scored at the tile it lands on, so a
rabbit chooses how far to walk.

```
weight = grassPull + hungerDrive / (1 + daysLeft)
score  = -weight * (distance from the tile it ends on to the nearest visible grass)
       + rabbitPull * sum over visible rabbits of 1 / (1 + distance from the tile it ends on)
```

Idling is scored at the rabbit's own tile. Breeding keeps it there too, and
scores as idling plus `breedPull`, so breeding competes with walking toward food
and with staying, and a rabbit far from grass usually walks instead.

`daysLeft` comes from the own view of `Metabolism`, computed from the reserve and
the live eating charge and interval. A term with nothing of its kind in view is
zero for every candidate. Rabbits means entities of the perceiver's own species,
read from `EntityView.species`. A second species gets a term of its own.

The pick is a softmax: each candidate's chance is proportional to
`exp(score / whim)`, with scores shifted by their maximum first. One draw from
the world's generator picks among the candidates in the order they are offered,
idling first, then the grid's directions in order, each at distances one up to
`stride`, then breeding.

| Gene | Bounds | Baseline | Mutation size |
|---|---|---|---|
| `viewRange` | 1 to 10 | 3 | 1.0 |
| `grassPull` | -5 to 5 | 1 | 0.2 |
| `hungerDrive` | -10 to 10 | 2 | 0.4 |
| `rabbitPull` | -2 to 2 | -0.2 | 0.1 |
| `whim` | 0.05 to 5 | 0.5 | 0.1 |
| `breedPull` | 0 to 5 | 1 | 0.2 |

## Consequences

Behaviour comes from weights rather than cases. Whether rabbits spread out or
herd is where `rabbitPull` drifts, and a fed rabbit wanders more than a starving
one because hunger raises the weight that `whim` divides, with no rule saying so.

The grass term is measured in tiles, so `whim` reads in tiles too. At a grass
weight of 1 and a `whim` of 1, a tile one step worse is e⁻¹ as likely as the
best.

With nothing in view every score ties, and the rabbit picks uniformly among its
candidates, which is how it wanders.

Every gene is a continuous nudge with a gradual effect, which is what 0018
wants from a gene under selection.

The pulls are signed, as `rabbitPull` always was. A negative grass weight
rewards tiles far from grass, so a rabbit that drifts there walks away from food
and starves, with no case in the score for it. A starting value near the middle
of a gene's bounds moves up or down by mutation equally often, so a gate can
read a rise as selection.

`whim` is likely to drift to its floor, since greedy is best when food is in view
and `whim` changes nothing when it is not. That is a finding rather than a
fault, and if watching shows rabbits need to explore, the answer is memory or
patchy food rather than a cost on `whim`.

`hungerDrive` has little to select on until breeding gives a reserve a use in
entry 6.

At the baseline `whim` of 0.5, a `breedPull` of 1 makes breeding about seven
times likelier than idling, and about as attractive as one tile closer to
grass. Its cost is built in: breeding spends the day's move and the reserve, so
a high `breedPull` makes thin parents. Nothing in the score keeps a margin, so
the world does, per [0004](0004-creatures-return-intents.md).

A rabbit between two equal patches can alternate between them, since nothing
remembers a target. `docs/ideas/brain-memory.md` is the fix when it matters.

While food lands uniformly, `rabbitPull` can only select on competition, so it
can only push rabbits apart. `docs/ideas/patchy-food.md` gives herding a payoff.

A predator adds a term and a gene, such as a pull toward wolves, rather than a
new strategy.

The score is a weighted sum of features followed by a softmax, which is a network
with one layer, so the network brain in 0018 can start from these exact weights
rather than from weights fitted to imitate them.

A brain test hands the brain a perception where one option clearly wins, or two
tie, and checks the pick over a seeded generator.

The step distribution is gone, and with it the only gene that needed a
distribution, per [0006](0006-genomes-are-a-named-layout-of-gene-shapes.md).
How far a rabbit can move is a setting, the same for every rabbit, until a
tradeoff makes it worth evolving, which `docs/ideas/gene-tradeoffs.md` keeps.
