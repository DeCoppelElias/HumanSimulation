# 8. Predator

Status: not started.

A second species whose brain hunts Rabbit. Violence arrives here as a new
intent. Rabbit's brain gains a flee response to a perceived threat component,
which it knows without knowing the predator species. This is the first test of
capabilities meeting without having been introduced.

The entry decides whether a kill leaves remains, since whatever kills spawns
them. `docs/ideas/corpses-and-scavenging.md` holds that question.

A predator that eats grass is not a predator, so this is where feeding stops
taking every edible. `docs/ideas/diet.md` holds how a creature says what it
eats.

Details to settle in this entry's grill: whether a predator can catch anything.
Every creature decides before anyone moves, and a move is a direction and a
distance rather than a target, so a wolf walks toward where a rabbit was, and a
fleeing rabbit as fast as the wolf may never be caught. The ways out are an
intent that names its target and is resolved against where the target stands
when the resolver reaches it, or deciding on each creature's turn, which
[0005](../decisions/0005-a-day-is-one-method.md) names as a change to one method.
Either changes the shape of a day or an intent, which `DESIGN.md` counts as a
design flaw to record, so the grill settles it before the hunting brain is
written.

On the page: a wolf with its own sprite, rabbits running from a wolf they can
see, and a kill that removes a rabbit at once.

Depends on entry 7. See
[0004](../decisions/0004-creatures-return-intents.md),
[0005](../decisions/0005-a-day-is-one-method.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0016](../decisions/0016-feeding-takes-the-tile.md).
