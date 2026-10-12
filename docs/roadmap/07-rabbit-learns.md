# 7. Rabbit learns

Status: not started.

A gate rather than a feature: selection has to be shown working before a second
species muddies it. A world setting turns mutation off, and the runner compares
mutating runs against that control across several seeds. `run` gains what that
needs: overriding any declared setting from the command line or a file, and a
batch over a range of seeds in one process, run in parallel, with a summary line
per seed.

The entry is done when mutating runs reliably outlast or outnumber the control,
the brain's genes drift the same way across seeds, and speed settles short of
its bound. This entry's grill turns each of those into a number, such as
mutation winning in at least eight of ten seeds by day 1000, and sets a timebox
for tuning. It also settles whether a gene's mutation size can be overridden
like a setting, since tuning it should not need a rebuild. Getting there is
tuning the default settings and food pressure until differences between rabbits
matter. If behaviour stays dull, widen what a rabbit perceives or can do before
reaching for the network brain, per `DESIGN.md`.

On the page: nothing new is required. What changes is behaviour you can see,
such as rabbits heading for food more directly after a few hundred days than at
the start.

Depends on entry 6. See
[0012](../decisions/0012-tests-target-brains-without-a-world.md),
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).
