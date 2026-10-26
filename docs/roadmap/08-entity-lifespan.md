# 8. Entity lifespan

Status: not started.

What ends an entity besides starving, per
[0024](../decisions/0024-a-part-can-end-its-carrier.md). Every component gains
a daily `tick` that does nothing by default and may end its carrier with a
cause. The day's fourth step becomes Tick, which ticks every part of every
entity in id order, rather than asking `Metabolism` by name. `Metabolism` ends
its carrier as `starved`, and the feed rule ends what it eats as `eaten`.

A new `Lifespan` part counts its carrier's days and ends it as `aged` when
they run out, at a fixed number of days set by the species' `lifespan`
setting, bounded 1 to 100. Grass declares it, so grass left uneaten withers
and food a population fails to find is lost. Rabbits do not declare it yet.
Its own view reports age and days left, which no brain reads.

The census row gains the day's deaths per species and cause, per
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md).
`compare.py` adds a deaths section per species, one row per cause giving its
share of the species' deaths, min, median and max across seeds, and a
comparison row per cause share, still knowing no cause by name. A species that
only ever dies of one cause gets no section. Grass appears in the report through its deaths. The
per-seed population table moves behind `--per-seed`, so the default report
stays short.

Withering changes how much grass the world holds, so the grass lifespan, from
12 days, and the food defaults are tuned together until both lines below on the
`watchable` preset hold.

Acceptance:

- grass left uneaten for its lifespan is gone, and grass eaten before then
  feeds as it does today;
- with the defaults and no ramp, rabbits are alive on day 1000 in at least 24
  of 30 seeds of the `watchable` preset;
- with the defaults, between 10% and 40% of grass deaths are `aged` rather
  than `eaten`, median over the 30 seeds of the `watchable` preset.

On the page: grass that disappears with no rabbit near it.

Depends on entry 7. See
[0003](../decisions/0003-entities-carry-components.md),
[0005](../decisions/0005-a-day-is-one-method.md),
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0024](../decisions/0024-a-part-can-end-its-carrier.md),
`docs/ideas/lifespan.md`.
