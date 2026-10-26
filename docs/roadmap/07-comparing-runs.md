# 7. Comparing runs

Status: done.

The tools an experiment needs to say whether one variant of a world did better
than another, and the finding that moved the selection gate to entry 9.

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

The report also compares each variant with the first on every measure, per
0021 and `docs/ideas/comparison-statistics.md`: both medians, the Â12 effect
size, and a Mann-Whitney p given raw and Holm-adjusted, with a line under the
table saying which to read when. Presets run seeds 1 to 30.

A spawn can override genes of the baseline for the members it creates, per
0007, and `run --gene species.gene=value` applies that to its starting spawn.
The HTTP API takes the overrides as an optional `genes` field on a spawn.

The first preset, now `mutation-ramp`, runs rabbits that mutate against clones
that cannot, both from a genome that barely aims for food, as grass per drop
falls from 7 to 1. It was meant to be the gate for selection, and ten tuning
runs showed it cannot be. Grass waits until it is eaten, so a population eats
every drop however well it forages, and survival in separate worlds cannot see
foraging skill. Competition inside a mutating population also raised speed and
view at a cost the whole population paid, which clones never pay. Entry 8 lets
uneaten grass wither, and entry 9 measures selection by letting evolved rabbits
compete against their ancestors. The tuning log is kept in the work plan. The
preset stays as a worked example of `--gene`, `--set-at` and two variants, and
its description says it does not measure selection.

Acceptance:

- `mutation-ramp` and `watchable` run and report with the statistics, and the
  statistics are tested against canned values;
- with the defaults and no ramp, rabbits are alive on day 1000 in at least 24
  of 30 seeds, so the world the page starts from stays watchable.

On the page: the World group shows the `mutation` checkbox.

Depends on entry 6. See
[0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0012](../decisions/0012-tests-target-brains-without-a-world.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md),
`docs/ideas/run-comparison.md`, `docs/ideas/comparison-statistics.md`.
