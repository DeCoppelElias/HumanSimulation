# 6. Breeding and settings

Status: not started.

`Breed` as an intent, with interval, cost and endowment as settings. The cost is
counted in days of reserve, per entry 5, so the 2022 cost of three becomes 45.
`GeneSpec` gains scalar mutation, which turns the baseline values from entries 1
and 5 into genes that vary and pass to a child: view range and the scoring
brain's genes from
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md), plus speed.
Speed and view range arrive with their metabolism charges, `speedCharge` and
`viewCharge`, since a gene that only helps pins to its bound. `run` reports gene
means.

2022 features that come back here: breeding on an interval when the reserve
covers the cost, every fifteen days at a cost of three by default, with the
child on its parent's tile; and mutation of every inherited trait. A child
starts from an endowment rather than the 2022 empty reserve.

Regression rules that land here: view range is inherited and varied, a view
range never drops below one, and a zero breeding interval is rejected. The two
2022 rules about the step distribution, that its variation is not integer
divided and that it stays valid, guard nothing since 0023 removed it.

Then the parameters panel, which 2022 also had, generated from declared
settings and grouped by species, with each setting's description beside it and
editing a setting as a command. Every world on a server holds the same species
objects, so this entry's grill settles where a world keeps its own setting
values, since an edit in one world must not reach the others. This entry's grill settles what
a world reset does with edited settings: keeping them means the new log no
longer replays from the seed alone, and reverting them loses the edits. It also
settles which settings can only be set when a world is created.

On the page: a newborn appears on its parent's tile, the population grows and
shrinks on its own, and a settings panel changes any declared number while the
world runs.

Depends on entry 5. See
[0002](../decisions/0002-replace-the-model-layer-in-place.md),
[0004](../decisions/0004-creatures-return-intents.md),
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md).
