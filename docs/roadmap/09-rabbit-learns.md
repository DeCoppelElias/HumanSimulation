# 9. Rabbit learns

Status: in progress.

A gate rather than a feature: selection has to be shown working before a second
species muddies it. Rabbits that evolved compete against the genome they
started from, and the world judges which lives better, so no gene is expected
to move any particular way.

`run` exports the genomes alive on given days and spawns a world from a file of
exported genomes, the part of `docs/ideas/brain-library.md` an experiment
needs, per [0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).
The snapshot carries each entity's genes, per
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md), which
the export reads. Saving a library, versioning layouts and loading genomes on
the page wait.

`grassPull` and `hungerDrive` become signed, -5 to 5 and -10 to 10, per
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md), so a
poor starting genome sits mid-range and mutation alone moves it up or down
equally often.

`tools/ancestor_competition.py` runs the gate from the preset
`tools/experiments/rabbit-learns-gate.json`, per
`docs/ideas/ancestor-competition.md`:

- Evolution: a 40×40 grid with 28 grass per drop, the default density once
  the start has tapered. 20 identical rabbits start from the poor genome,
  `grassPull` 1.6, `hungerDrive` 0.5 and `whim` 2 with every other gene at
  its baseline, and evolve for 2000 days with mutation on, exporting on days
  1000 and 2000. Tuning chose `grassPull` 1.6 because ten such rabbits alone
  at the default density survive 500 days in about half the seeds. Poor
  founders still die out in half the seeds at that density, so grass drops
  every day at first, then every 2, 3, 5 and 7 days from days 400, 800, 1200
  and 1600, which tuning found lets learning keep up with the food.
- Sample: 10 of a seed's day 2000 survivors that differ from the original
  genome, since rabbits do not age and a founder can still be alive, drawn
  with a seed the tool records. A seed that died out or has fewer than 10 is
  not tested.
- Test: mutation off, on the evolution seed plus 1000, for 500 days, in the
  evolution world with grass every 3 days. One world holds the 10 evolved
  beside 10 of the original genome, and each group also runs in a world of its
  own. At the default density the original genome alone holds about 2 rabbits,
  so a smaller evolved population could not show, and with more food it holds
  about 60. Each test run exports its
  survivors on its last day, and an exact genome says which group a rabbit
  descends from.
- Report: the seeds tested and why the others were not, the sample seed, the
  time the gate took, the evolved share of living rabbits in the shared world,
  the population each group sustains in a world of its own as a mean over
  every day of the test, and the evolved genes on each export day against the
  original genome. It never judges, and `--per-seed` and
  `--json` work as in `compare.py`.

Acceptance, on the gate's report over seeds 1 to 30:

- at least 24 seeds are tested;
- the evolved share of living rabbits on day 500 of the shared world is above
  one half in at least 70% of the tested seeds;
- the evolved group in a world of its own sustains fewer rabbits than the
  original genome in under 70% of the tested seeds, a guard that fails only on
  clear evidence of a smaller population;
- `run` spawns every line of an exported file, and a world spawned from an
  export with mutation off holds exactly the exported genomes.

Tuning changes only the preset: a gentler start, the world's size with its food
at the same density, the group sizes, the two lengths, the test world's food,
and the poor genome while each poor gene stays mid-range. The marks, the design, the bounds and the
mutation sizes stay. The budget is 15 runs, each logged with the symptom it
addressed and the time the gate took. Once the gate passes, it runs once on
seeds 31 to 60 and the result is logged for Elias. If the budget runs out
without a pass, Elias gets the log, the last report with `--per-seed` and a
proposal, and nothing merges until he decides.

Where the brain's genes drift is reported per seed with no pass mark. Elias
reads it before the entry is done, so a gate passed only on cheaper bodies, or
on behaviour he does not want, is caught by a person rather than a threshold.
The mutation shadow stays out, since the signed bounds leave no floor for a
gene to climb off. The network brain waits, per `DESIGN.md`.

On the page: a world built over the API like the gate's evolution stage,
opened at `?world=ID`, with the drop interval stepped up from the settings
panel as the gate's taper does, where rabbits stumble about in the first weeks
and head for grass more directly after a few hundred days. A 20×20 world with
7 grass per drop and 10 rabbits is the fallback if 40×40 tiles are too small
to follow.

Depends on entry 8. See
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0012](../decisions/0012-tests-target-brains-without-a-world.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md),
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md),
`docs/ideas/ancestor-competition.md`, `docs/ideas/brain-library.md`,
`docs/ideas/mutation-shadow.md`.
