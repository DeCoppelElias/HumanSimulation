# 8. Entity lifespan

Status: not started.

What ends an entity besides starving. A component counts its carrier's days and
ends the carrier when they run out, as `Metabolism` ends a carrier whose
reserve falls below zero. A species that declares it sets how long its members
last with a setting of its own. Grass declares it, so grass left uneaten
withers and food a population fails to find is lost. The step that removes the
dead asks every part that can end its carrier, rather than `Metabolism` by
name.

Nothing in the decision log says what ends an entity, so a new entry records
it before this is built, and `DESIGN.md`'s day changes with it.

Withering changes how much grass the world holds, so the food defaults are
retuned until the world the page starts from stays watchable.

Details to settle in this entry's grill: whether a lifespan is a fixed number
of days or a daily chance, whether rabbits declare it in this entry and with
what default, whether the census counts deaths by cause, grass's default
lifespan, and whether a creature perceives its own age.

Acceptance:

- grass left uneaten for its lifespan is gone, and grass eaten before then
  feeds as it does today;
- with the defaults and no ramp, rabbits are alive on day 1000 in at least 8 of
  10 seeds of the `watchable` preset.

On the page: grass that disappears with no rabbit near it.

Depends on entry 7. See
[0003](../decisions/0003-entities-carry-components.md),
[0005](../decisions/0005-a-day-is-one-method.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
`docs/ideas/lifespan.md`.
