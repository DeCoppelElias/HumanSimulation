# 11. Predator

Status: done.

Entry 9's gate passed unchanged under the new brain, with the evolved group
ahead in all 30 seeds. On the predator preset wolves were alive on day 200 in
26 of 30 seeds, with a median of 9.1 wolves, and rabbits on day 2000 in 24.

A second species that eats rabbits. Rabbits carry `Meat`, worth the rabbit's
`nourishment` setting of 30 where grass is worth 15, and a wolf carries an
`Eater` that takes it. There is no attack intent, per
[0004](../decisions/0004-creatures-return-intents.md). A kill leaves no
remains, and `docs/ideas/corpses-and-scavenging.md` holds what remains would
add. Rabbits do not flee yet, which is entry 12, so this entry shows whether a
wolf can hunt at all.

A creature eats at the end of its own turn in the resolve step, and the feed
step goes, per [0005](../decisions/0005-a-day-is-one-method.md) and
[0016](../decisions/0016-feeding-takes-the-tile.md). A wolf whose turn comes
before a rabbit's lands on its tile and eats it before it leaves, so speed is
the chase.

The scoring brain becomes a sum over features, per
[0013](../decisions/0013-perception-is-one-type.md) and
[0023](../decisions/0023-creatures-score-the-tiles-they-can-reach.md). Each
component lists the features it shows, the catalog holds `Plant`, `Meat`,
`takes.Plant` and `takes.Meat`, and `Meat` and `Eater` open their seen views.
Every species with the brain carries `near.X`, `hungry.near.X` and `sum.X` for
each feature, twelve pull genes that replace `grassPull`, `hungerDrive` and
`rabbitPull`. The rabbit's baselines give its old scores. The wolf is a copy of
the rabbit except for its `Eater`, its pull baselines and its sprite, with
speed 0 to 1 at 0.5 and a stride of 3 like the rabbit's, so the race starts
even.

`run` and `serve` know the wolf, and their default worlds stay rabbit-only, so
`tools/experiments/watchable.json` and entries 7 and 8 keep their meaning. The
wolf's sprite is a wolf, free to use, credited in
`frontend/public/sprites/LICENSE.txt` as its licence asks.

Acceptance:

- a wolf whose turn comes before a rabbit's on its tile eats it, and one whose
  turn comes after finds the tile empty;
- the brain is pulled by a feature it sees, tested with a perception and no
  world, and at the rabbit's baselines scores the tiles of a fixed perception
  as the old brain did;
- a test fails when a component shows a feature the catalog lacks;
- entry 9's gate, its preset renamed to the new genes with every value and mark
  kept, passes on seeds 1 to 30. A failure gets 5 runs of tuning under entry
  9's rules, then Elias gets the log and the report with `--per-seed`, and
  nothing merges until he decides;
- `tools/experiments/predator.json` runs a 40×40 world with 28 grass every 3
  days, 40 rabbits and 6 wolves, mutation on, for 2000 days on seeds 1 to 30.
  Wolves are alive on day 200 in at least 24 seeds, the median wolf population
  over the days they live is at least 5, and rabbits are alive on day 2000 in
  at least 24 seeds. Wolves and rabbits living side by side for the whole run
  is entry 12's, since rabbits that cannot flee lose the speed race and leave
  wolves to starve, and richer wolves eat the rabbits out.

Where the wolves' `speed` and `near.Meat` and the rabbits' `speed` drift is
reported with no mark, and Elias reads it before the entry is done.

Tuning the predator preset changes only the wolf's settings, the rabbit's
`nourishment`, the starting counts and the world's food, never the marks, the
brain or the bounds. The budget is 15 runs, each logged with the symptom it
addressed. If it runs out without a pass, Elias gets the log, the last report
with `--per-seed` and a proposal, and nothing merges until he decides.

On the page: a world built over the API, 25×25 with grass at the predator
preset's density, 25 rabbits and 2 wolves, opened at `?world=ID`. A wolf with
its own sprite ignores grass and walks toward rabbits it can see, and a rabbit
disappears when a wolf reaches its tile. Nobody expects that world to stay
balanced.

Depends on entry 10. See
[0004](../decisions/0004-creatures-return-intents.md),
[0005](../decisions/0005-a-day-is-one-method.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0016](../decisions/0016-feeding-takes-the-tile.md),
[0023](../decisions/0023-creatures-score-the-tiles-they-can-reach.md),
`docs/ideas/corpses-and-scavenging.md`, `docs/ideas/kin-recognition.md`,
`docs/ideas/network-brain.md`.
