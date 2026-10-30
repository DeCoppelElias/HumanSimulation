# 13. Inspecting creatures

Status: done.

Looking at one creature closely. The values a person sees come from the
snapshot, where every component declares what it shows, per
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md):
`Lifespan` shows `age` and `daysToLive`, `Metabolism` shows `reserve` and
`daysToStarve`, and `Plant` and `Meat` show `amount`. Rabbits and wolves carry
`Lifespan` with no length set, so it counts their age and never ends them, per
[0024](../decisions/0024-a-part-can-end-its-carrier.md). Nothing draws from the
generator for it, so every seeded run, entry 9's and 12's gates and the sample
in `README.md` stay as they are.

A toggle beside the grid sets what a click does, inspect or spawn, and starts
on inspect. An inspect click on a tile holding one entity selects it. On a tile
holding more, grass included, it lists what stood there at the click, and the
list stays as it was until the next click. Picking a row selects that entity.

The selected entity's panel follows it live by id: its values and genes from
the latest day, and for a creature a light tint over the tiles its `viewRange`
gene lets it see. The page computes that circle the way the grid does, rounding
the gene and keeping tiles whose squared distance is within its square, and
draws sight only.

A table lists every creature with its species, age, reserve and days to starve,
sorts by age or by reserve, largest first, and refreshes only when its button is
pressed. Clicking a row selects that creature.

The selection clears when the selected id stops resolving, per
[0009](../decisions/0009-terrain-is-entities-plus-tile-fields.md), whether the
creature died while selected or was picked from a stale list, and the panel
then says it is no longer alive. A reset clears it silently.

2022 features that come back here: a creature's age, the list of humans sorted
by days survived or by food, selecting a human to see its information and view
range, and clicking a tile to see its contents.

API tests cover that the snapshot carries each creature's age, reserve and days
to starve, grass its amount and days to live, and no days to live on a
creature. A domain test covers that a creature carrying `Lifespan` with no
length set outlives any length a species could set. Vitest covers the selection
emptying when its id stops resolving, the tile list, the table's sort and the
sight circle against the grid's own cases.

On the page: clicking a rabbit on its own shows its age, reserve and genes and
tints the tiles it can see, clicking a crowded tile lists what is on it, the
panel says when the selected rabbit has died, and the table sorts by age or
reserve and refreshes on its button.

Depends on entry 8. See
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0009](../decisions/0009-terrain-is-entities-plus-tile-fields.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0020](../decisions/0020-the-interface-is-a-web-page.md),
[0024](../decisions/0024-a-part-can-end-its-carrier.md).
