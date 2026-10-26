# 11. Inspecting creatures

Status: not started.

Looking at one creature closely. Clicking a tile shows what stands on it.
Selecting a creature shows its age in days, its reserve and its genes, and
draws its view range on the grid. A list of every creature sorts by age or by
reserve. The selection clears when the selected id stops resolving, per
[0009](../decisions/0009-terrain-is-entities-plus-tile-fields.md).

2022 features that come back here: a creature's age, the list of humans sorted
by days survived or by food, selecting a human to see its information and view
range, and clicking a tile to see its contents.

API tests cover what a selection returns, and that a selection whose id has
stopped resolving comes back empty rather than failing.

Details to settle in this entry's grill: a click on a tile already spawns a
rabbit from entry 3, and food from entry 5, so inspecting needs its own way in,
such as a mode or a modifier key.

On the page: clicking a rabbit shows its age, reserve and genes and outlines
the tiles it can see, and the list sorts by age or reserve.

Depends on entry 6. See
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0020](../decisions/0020-the-interface-is-a-web-page.md).
