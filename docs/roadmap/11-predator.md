# 11. Predator

Status: not started.

A second species whose brain hunts Rabbit. Violence arrives here as a new
intent. Rabbits do not flee yet, which is entry 12, so this entry shows whether
a wolf can hunt at all before prey fights back.

A kill leaves no remains. `docs/ideas/corpses-and-scavenging.md` holds what
remains would add, and stays an idea until the predator works.

Details to settle in this entry's grill: whether a predator can catch anything.
Every creature decides before anyone moves, and a move is a direction and a
distance rather than a target, so a wolf walks toward where a rabbit was. The
ways out are an intent that names its target and is resolved against where the
target stands when the resolver reaches it, or deciding on each creature's
turn, which [0005](../decisions/0005-a-day-is-one-method.md) names as a change
to one method. Either changes the shape of a day or an intent, which
`DESIGN.md` counts as a design flaw to record, so the grill settles it before
the hunting brain is written. The grill also settles what a kill gives the
wolf, and the acceptance lines: wolves that sustain themselves on rabbits over
a seed range without wiping them out.

On the page: a wolf with its own sprite that ignores grass, walks toward
rabbits it can see, and a kill that removes a rabbit at once.

Depends on entry 10. See
[0004](../decisions/0004-creatures-return-intents.md),
[0005](../decisions/0005-a-day-is-one-method.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0016](../decisions/0016-feeding-takes-the-tile.md),
`docs/ideas/corpses-and-scavenging.md`.
