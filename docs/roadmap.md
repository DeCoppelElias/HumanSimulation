# Roadmap

The order the model layer is being rebuilt in. `DESIGN.md` describes the target
architecture; this says which parts of it exist yet and what's next. Status
lives here as a field on each entry rather than as a separate description of
what the code contains, which would duplicate the code and go stale the same
way.

Entries are added as they're planned and their status is flipped as they land.
Read `docs/decisions/` alongside this: a decision log entry explains why
something is shaped the way it is, this says whether it exists yet.

## 1. Minimal watchable loop

Status: not started.

The type skeleton only: `World`, `Entity`, `Species`, `Component`, `Genome`,
and the day loop's six steps (decide, resolve, feed, metabolise, world
processes, clean up), wired into the existing GUI through a snapshot. Rabbit
entities exist but their brain always returns `Idle`. No real behaviour yet.
This proves the architecture runs and renders before anything is asked to be
smart.

See [0002](decisions/0002-replace-the-model-layer-in-place.md),
[0004](decisions/0004-creatures-return-intents.md),
[0005](decisions/0005-a-day-is-an-ordered-list-of-systems.md),
[0007](decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0010](decisions/0010-runs-replay-exactly-from-a-seed.md).

## 2. Rabbit forages

Status: not started.

Grass as an `Edible` entity, `Metabolism` and hunger, and the fixed-rules
brain that seeks the nearest food. Breeding is a decision a brain makes, not a
system. This is the first generation-over-generation selection loop: the
population drifting toward whatever finds food best.

Depends on entry 1. See
[0018](decisions/0018-brain-computation-model-and-warm-starting.md).

## 3. Predator

Status: not started.

A second species whose brain hunts Rabbit. Violence arrives here as a new
intent, per [0004](decisions/0004-creatures-return-intents.md). Rabbit's brain
gains a flee response to a perceived threat component, which it knows without
knowing the predator species. This is the first test of capabilities meeting
without having been introduced.

Depends on entry 2.
