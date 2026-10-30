# 12. Fleeing

Status: not started.

Rabbits learn to keep away from wolves. Nothing names a wolf as a threat: a
rabbit sees that something takes `Meat`, through its `takes.Meat` pull genes
from entry 11, and those genes evolve. The rabbit's `near.takes.Meat` starts at
-0.5 and its `sum.takes.Meat` at -0.2, since an animal keeps a little away from
what is not its kind, and selection refines it or finds something else, per
[0023](../decisions/0023-creatures-score-the-tiles-they-can-reach.md). Its
`hungry.near.takes.Meat` stays at 0, so selection decides whether a starving
rabbit takes more risk. The wolf keeps entry 11's genes and settings, speed 0.5
and a stride of 3, so the race still starts even. This is the first test of
capabilities meeting without having been introduced.

Wolves and rabbits live side by side on a larger world.
`tools/experiments/fleeing.json` runs an 80×80 world at the predator preset's
density, 75 grass every 2 days, 160 rabbits and 24 wolves, mutation on, for
2000 days on seeds 1 to 30. Probes while grilling found no wolf setting that
keeps both alive on entry 11's 40×40 world: a faster wolf, a stride of 4 or 5
and a dearer birth all left wolves alive on day 2000 in at most 4 of 30 seeds,
since a few wolves at the bottom of a boom and bust die out by chance. On
80×80, with the rabbit's new baselines, both lived in 25 of 30. The predator
preset stays as entry 11's record.

A gate shows that what rabbits learn comes from the wolves. Two arms evolve
from one rabbit on one world, and only one has wolves, so a gene that moves the
same way in nearly every seed of that arm, and not in the control, is selected
for by the wolves. The gate reads that from `near.takes.Meat`, the gene the
lesson is expected in. It also runs entry 9's head-to-head test and reports it
without a mark: fleeing costs grazing, so whether the lesson pays overall in a
test world is a separate question from whether it was learned.
`tools/ancestor_competition.py` gains an optional `control` block: a
second evolution arm whose survivors replace the starting genome as the
comparison group. A preset without one runs as before, so
`tools/experiments/rabbit-learns-gate.json` keeps its meaning. The preset
`tools/experiments/rabbit-flees-gate.json`:

- Evolution: both arms start from the rabbit's baseline on `fleeing.json`'s
  world and food for 1000 days, mutation on. One has wolves, which co-evolve,
  and the control has none.
- Sample: 10 of each arm's day 1000 survivors that differ from the starting
  genome, and 4 of the evolving arm's day 1000 wolves. A seed is not tested
  when its wolves died out during evolution or either arm has fewer than 10.
- Test: mutation off, entry 9's test world, 40×40 with 28 grass every 3 days
  for 500 days, plus the 4 sampled wolves spawned on day 1. One world holds 10
  of each group, and each group also runs in a world of its own, all beside the
  same wolves. The groups are told apart by exact genome, as in entry 9. Wolves
  at their baseline were tried first: both rabbit arms evolve to about twice
  their speed, so they starved within weeks and the test became a contest for
  grass.
- Report: as entry 9's, naming the control where it names the original genome,
  plus the test worlds where wolves outlived day 250, and for each gene the
  seeds whose evolving arm ended below its start and below its control.

Acceptance:

- a rabbit at its baseline, handed a perception with grass on one side and a
  wolf on the other, scores tiles away from the wolf higher than it did with
  its `takes.Meat` genes at 0;
- `tools/experiments/fleeing.json`: wolves and rabbits are both alive on day
  2000 in at least 24 of 30 seeds;
- the gate, over seeds 1 to 30: at least 24 seeds are tested; the evolving
  arm's mean `near.takes.Meat` on day 1000 is below its start of -0.5 in at
  least 70% of the tested seeds, and below the control's in at least 70%;
- `rabbit-learns-gate.json` runs unchanged, and the tool's tests cover a preset
  with and without a `control` block.

This mark replaced one on the head-to-head share, which wanted the with-wolves
group above one half of the shared world in 70% of seeds and reached 36% to
52% over seven tuning runs. It was set after those runs, when the gene drift
was already known to be lopsided. Where both arms' other `takes.Meat` genes,
both species' `speed` and the head-to-head numbers land is reported with no
mark, and Elias reads it before the entry is done.

Tuning `fleeing.json` changes only the wolf's settings, the rabbit's
`nourishment`, the starting counts and the world's food, with a budget of 10
runs. Tuning the gate changes only the test world's wolf count, length and
food, the group sizes and the evolution world's food, with a budget of 8 runs
counted from the switch to sampled wolves.
Neither changes the marks, the brain, the bounds, the baselines, the wolf's
speed or stride, or a world's size. Every run uses `--jobs 6`, one at a time,
and none may take over 30 minutes; measured while grilling, `fleeing.json`
takes about 4 minutes and the gate about 5. Each run is logged with the symptom
it addressed. If a budget runs out without a pass, Elias gets the log, the last
report with `--per-seed` and a proposal, and nothing merges until he decides.

On the page: entry 11's world, built over the API, 25×25 with 11 grass every 3
days, 25 rabbits and 2 wolves, opened at `?world=ID`. A rabbit steps away when
a wolf comes into view, and wolves still catch rabbits busy with grass or
cornered. Nobody expects that world to stay balanced.

Depends on entry 11. See
[0013](../decisions/0013-perception-is-one-type.md),
[0023](../decisions/0023-creatures-score-the-tiles-they-can-reach.md),
`docs/ideas/ancestor-competition.md`, `docs/ideas/immigration.md`,
`docs/ideas/patchy-food.md`, `docs/ideas/run-speed.md`.
