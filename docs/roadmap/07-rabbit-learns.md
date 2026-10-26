# 7. Rabbit learns

Status: not started.

A gate rather than a feature: selection has to be shown working before a second
species muddies it. Rabbits that mutate are run against clones that cannot, in a
world that gets harsher, and the ones that can change should hold on longer.

The world declares its first setting of its own, `mutation`, a toggle that is on
by default. Off, a child is an exact copy of its parent and a spawn an exact copy
of the baseline. `Setting` gains its toggle shape, stored as 0 or 1, and the
panel draws it as a checkbox. Its description says that unticking it stops the
world evolving.

`run` gains `--set owner.key=value`, repeatable, sent as a `SetSetting` on day 0,
and `--set-at DAY:owner.key=value`, sent once DAY days are complete. A bad key or
value fails with the message a panel edit would get.

A comparison script in `tools/` runs variants of a world over a range of seeds,
one `run` per seed and variant in parallel, and reports, per species, the day
each world died out, its mean and peak population, and each gene's mean against
its mean on day 1. Gene means are taken on the last day a world had at least
five members of the species. The text report gives counts across seeds, such as
how many seeds one variant outlasted another, and `--json` and `--per-seed` give
everything. It knows no species, gene or setting by name, passes `run` flags
through, and never judges. Its logic is tested against canned census lines, and
those tests run in CI. Presets live in `tools/experiments/`, and flags override
them.

A spawn can override genes of the baseline for the members it creates, per
0007, and `run --gene species.gene=value` applies that to its starting spawn.
The HTTP API takes the overrides as an optional `genes` field on a spawn.

The first preset is `selection-gate`: a control with `world.mutation=0` and a
mutating variant on the defaults, seeds 1 to 10, 1000 days, `run`'s default grid
and rabbits, and both variants lowering `world.grassPerDrop` by 1 every 100 days
from 7 to 1. Both start from a poor genome that barely aims for food, at first
`grassPull` 0.2, `hungerDrive` 0.5 and `whim` 2, so the gate asks whether
evolution recovers from a bad start. The first eight tuning runs found that it
cannot beat the species' own baseline, which is already near the best this world
rewards: the clones held a larger population, and racing for grass pushed the
mutating rabbits' speed and view up at any charge.

Getting there is tuning, in at most 30 gate runs, each logged in the plan with
what changed and what it showed. Tuning may change the default settings, the
ramp, the poor genome in the preset, and the mutation sizes in the gene layout.
Gene bounds and the species' baselines stay. Defaults that 0014 or 0023 record
are rewritten there when tuning settles them. If 30 runs pass without
the gate passing, tuning stops, and the tuning log and last report go to Elias
with a proposal for widening what a rabbit perceives or can do, such as
`docs/ideas/patchy-food.md` or `docs/ideas/brain-memory.md`, as a new entry
before entry 8. The toggle, `--set`, `--set-at`, `--gene` and the script are
merged either way. The network brain waits, per `DESIGN.md`.

Acceptance, on the `selection-gate` report:

- the mutating world dies out later than the control in at least 8 of 10
  seeds, where a tie, including both alive on day 1000, is not later;
- mean speed in the mutating runs stays between 0.1 and 0.9 in at least 8 of
  10 seeds;
- with the tuned defaults and no ramp, rabbits are alive on day 1000 in at
  least 8 of 10 seeds, so the world the page starts from stays watchable.

Where the brain's genes drift is reported per seed with no pass mark. Elias
reads it before the entry is done, so a gate passed only on cheaper bodies, or
on behaviour he does not want, is caught by a person rather than a threshold.

On the page: the World group shows the `mutation` checkbox. Otherwise what
changes is behaviour you can see, such as rabbits heading for food more
directly after a few hundred days than at the start.

Depends on entry 6. See
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0012](../decisions/0012-tests-target-brains-without-a-world.md),
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md),
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md),
`docs/ideas/run-comparison.md`.
