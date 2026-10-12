# 5. Eating and starving

Status: not started.

Grass as an `Edible` entity, and a world process that spawns it. The feed and
metabolise steps and the list of world processes join the day here. `Metabolism`
holds a reserve its carrier can see and its neighbours cannot. The feed step
takes every edible on the tile, and the metabolise step charges on an interval
and removes anything whose reserve falls below zero. Intervals and charges join
the settings as species settings, and how much food arrives and how often join
as world settings, with bounds that reject non-positive values. `Setting`
arrives here with its first shape, which this entry's grill settles. That is
where the regression rule rejecting a zero eating interval lands, and the rule
that removing an entity validates before it mutates, since starvation is the
first thing that removes one.

Rabbit gets the fixed-rules brain that approaches the nearest edible. Its rule
set is a design decision per
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md), so it gets
a log entry before it is built. Its parameters sit in the baseline genome as
plain values until entry 6. The rule set's log entry also settles whether
walking away from a crowd survives, which decides whether the inverted crowd
comparison rule lands or goes to `docs/ideas/`.

A creature standing on grass is now the normal case, so the grid draws a
creature above the ground cover it stands on, and the count from entry 3
counts creatures rather than grass.

2022 features that come back here: food arriving on an interval in a set
amount, seven pieces every seven days by default; a reserve charged on an
interval, one every fifteen days by default, with death below zero; spawning
food by count and by clicking a tile; and a gene weighing wandering against
walking to food. Of the four 2022 food choices, the rule set keeps the closest
edible and a random visible one, and decides whether the ones farthest from
other creatures and nearest other food come back. It also decides the order in
which a move toward a target closes the two axes, which 2022 did x first. The
2022 split of a food between everyone who reached it is replaced by taking the
tile in speed order, per [0016](../decisions/0016-feeding-takes-the-tile.md).

On the page: grass with its own sprite appears every few days, drawn under any
rabbit on its tile. Rabbits head for grass they can see instead of only
wandering, and a rabbit that starves fades out. The chart rises and falls with
the food. Food can be spawned by count and by click.

Depends on entry 3. See
[0009](../decisions/0009-terrain-is-entities-plus-tile-fields.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0016](../decisions/0016-feeding-takes-the-tile.md),
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md).
