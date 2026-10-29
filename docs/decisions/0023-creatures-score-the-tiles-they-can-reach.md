# 0023. Creatures score the tiles they can reach

## Status

Accepted, 2026-10-12. Revised 2026-10-29.

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

The brain first named what it scored: `grassPull` and `hungerDrive` on the
nearest grass, and `rabbitPull` on summed closeness to other rabbits, with a
term and a gene of its own to come for each new species. A predator made that a
term per pair of species, and [0013](0013-perception-is-one-type.md) says why
the brain now sums over features instead.

A sum over features has to measure every feature the same way, since it cannot
know which one is food. Summed closeness for everything weakens food-seeking at
range: with grass 6 tiles away, moving 3 toward it scores `1/4 - 1/7`, about 0.11,
better, about 1.2 times likelier than staying at a `whim` of 0.5, where the
nearest distance scores it 3 better and makes it about 400 times likelier.
Nearest distance for everything loses counts, so five neighbours weigh what one
does, and avoiding the nearest pushes a creature to the edge of its view.
Choosing a shape per feature in the catalog would tell the brain what a feature
means. Both shapes for every feature let selection weigh each, and they contain
the brain that came before, so its baselines carry over unchanged.

A hunger version of every pull doubles the summed genes for uses nothing has
yet, such as hunger changing how much a creature minds a crowd. One hunger gene
scaling every pull makes a starving rabbit avoid crowds harder too, and tangles
eating only when hungry with everything else. Hunger on the nearest pulls is
where walking to or away from something is decided.

A rabbit's pulls on what a wolf shows could start at 0, so that keeping away
from wolves is wholly learned. Selection gets there on its own: on entry 11's
predator preset `near.takes.Meat` drifted from 0 to -0.37 by day 2000. An
animal keeps a little away from what is not its kind, though, and a neutral
start drops that. Starting at -1, with `sum.takes.Meat` at -0.5, is about where
selection settles, since by day 2000 it had moved down in only half the seeds,
so a gene report shows little being learned. The wolf could keep away from
`takes.Plant` in the same way, but every rabbit shows `takes.Plant` beside
`Meat`, so an aversion of 0.3 weakens the wolf's pull toward the nearest rabbit
by 30%, which is the wolf's whole job.

## Decision

Each day a creature with the scoring brain scores every tile it could land on
and picks one. Every species uses the same brain, and its genes are what
differ.

The candidates are the options it is offered, per
[0004](0004-creatures-return-intents.md): idling, each of the grid's directions
at each distance from one up to `stride`, a species setting with a default of 3,
and breeding when it is legal. A move is scored at the tile it lands on, so a
creature chooses how far to walk.

The score sums over the features other entities in view show, per
[0013](0013-perception-is-one-type.md), and names no component. Each feature
`X` in the catalog has three pulls: `near.X` on the distance to the nearest
entity showing it, `hungry.near.X` added to that pull in proportion to hunger,
and `sum.X` on summed closeness to every entity showing it.

```
urgency = 1 / (1 + daysLeft)
score   = sum over features X with something in view showing X of
            -(near.X + hungry.near.X * urgency) * (distance from the tile it ends on to the nearest one)
            + sum.X * sum over them of 1 / (1 + distance from the tile it ends on)
```

Idling is scored at the creature's own tile. Breeding keeps it there too, and
scores as idling plus `breedPull`, so breeding competes with walking toward food
and with staying, and a creature far from food usually walks instead.

`daysLeft` comes from the own view of `Metabolism`, computed from the reserve and
the live eating charge and interval. A feature with nothing in view showing it
adds zero for every candidate. The brain reads only other entities, so it never
reads its own diet: a rabbit's baselines pull it toward `Plant` and a wolf's
toward `Meat`, and selection keeps or changes that.

The pick is a softmax: each candidate's chance is proportional to
`exp(score / whim)`, with scores shifted by their maximum first. One draw from
the world's generator picks among the candidates in the order they are offered,
idling first, then the grid's directions in order, each at distances one up to
`stride`, then breeding.

Every species with the scoring brain carries one pull gene of each shape for
every feature in the catalog, in catalog order, with these bounds:

| Gene | Bounds | Mutation size |
|---|---|---|
| `near.X` | -5 to 5 | 0.2 |
| `hungry.near.X` | -10 to 10 | 0.4 |
| `sum.X` | -2 to 2 | 0.1 |

A pull gene is 0 unless its species gives a baseline. The other genes and the
baselines:

| Gene | Bounds | Mutation size | Rabbit | Wolf |
|---|---|---|---|---|
| `viewRange` | 1 to 10 | 1.0 | 3 | 3 |
| `whim` | 0.05 to 5 | 0.1 | 0.5 | 0.5 |
| `breedPull` | 0 to 5 | 0.2 | 1 | 1 |
| `near.Plant` | | | 1 | 0 |
| `hungry.near.Plant` | | | 2 | 0 |
| `sum.Meat` | | | -0.2 | 0 |
| `near.Meat` | | | 0 | 1 |
| `hungry.near.Meat` | | | 0 | 2 |
| `near.takes.Meat` | | | -0.5 | 0 |
| `sum.takes.Meat` | | | -0.2 | -0.2 |

Away from wolves, the rabbit's baselines give the scores of the brain it had
before features, so it starts each run behaving as it did. Its `takes.Meat`
pulls keep it somewhat away from anything that eats meat: a tile one step
farther from the nearest wolf is worth half what a step toward grass is worth
to a fed rabbit. The wolf's are the rabbit's food pulls aimed at rabbits and its
crowd pull at other wolves, so a wolf differs from a rabbit only where it is a
predator.

## Consequences

Behaviour comes from weights rather than cases. Whether rabbits spread out or
herd is where `sum.Meat` drifts, and a fed rabbit wanders more than a starving
one because hunger raises the weight that `whim` divides, with no rule saying so.

The nearest terms are measured in tiles, so `whim` reads in tiles too. At a
`near.Plant` of 1 and a `whim` of 1, a tile one step worse is e⁻¹ as likely as the
best.

With nothing in view every score ties, and the rabbit picks uniformly among its
candidates, which is how it wanders.

Every gene is a continuous nudge with a gradual effect, which is what 0018
wants from a gene under selection.

The pulls are signed. A negative `near.Plant`
rewards tiles far from grass, so a rabbit that drifts there walks away from food
and starves, with no case in the score for it. A starting value near the middle
of a gene's bounds moves up or down by mutation equally often, so a gate can
read a rise as selection.

`whim` is likely to drift to its floor, since greedy is best when food is in view
and `whim` changes nothing when it is not. That is a finding rather than a
fault, and if watching shows rabbits need to explore, the answer is memory or
patchy food rather than a cost on `whim`.

Another rabbit shows `Meat` and `takes.Plant` together, so a rabbit's
`sum.Meat` and `sum.takes.Plant` act only as their sum. The two can drift apart
with no change in behaviour, and the sum mutates by about 0.14 a generation
where one gene alone would by 0.1. A gene report shows both moving where one
thing is learned.

At the baseline `whim` of 0.5, a `breedPull` of 1 makes breeding about seven
times likelier than idling, and about as attractive as one tile closer to
grass. Its cost is built in: breeding spends the day's move and the reserve, so
a high `breedPull` makes thin parents. Nothing in the score keeps a margin, so
the world does, per [0004](0004-creatures-return-intents.md).

A rabbit between two equal patches can alternate between them, since nothing
remembers a target. `docs/ideas/brain-memory.md` is the fix when it matters.

While food lands uniformly, `sum.Meat` can only select on competition, so it
can only push rabbits apart. `docs/ideas/patchy-food.md` gives herding a payoff.

A new species meets the others through the features they show, rather than
through a new strategy. A rabbit's reaction to wolves is its `takes.Meat` genes,
which start somewhat negative and evolve, and a wolf whose
`near.Plant` rises waits at grass where rabbits arrive, which nothing wrote.

The score is a weighted sum of features followed by a softmax, which is a network
with one layer, so the network brain in 0018 can start from these exact weights
rather than from weights fitted to imitate them.

A brain test hands the brain a perception where one option clearly wins, or two
tie, and checks the pick over a seeded generator.

The step distribution is gone, and with it the only gene that needed a
distribution, per [0006](0006-genomes-are-a-named-layout-of-gene-shapes.md).
How far a rabbit can move is a setting, the same for every rabbit, until a
tradeoff makes it worth evolving, which `docs/ideas/gene-tradeoffs.md` keeps.
