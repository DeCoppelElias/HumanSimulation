# 11. Predator

Status: not started.

A second species that eats rabbits. Rabbits carry `Meat`, and a wolf carries an
`Eater` that takes it, so a wolf on a rabbit's tile eats it in the feed step,
per [0016](../decisions/0016-feeding-takes-the-tile.md). There is no attack
intent, per [0004](../decisions/0004-creatures-return-intents.md). A kill
leaves no remains. `docs/ideas/corpses-and-scavenging.md` holds what remains
would add. Rabbits do not flee yet, which is entry 12, so this entry shows
whether a wolf can hunt at all.

The scoring brain becomes one sum over what a creature sees. Each visible fact
about an entity, such as being the same species, carrying `Plant` or `Meat`, or
taking `Plant` or `Meat`, has a signed pull gene of its own, weighted by how
close the fact would be from the tile an option ends on. A fact may also come in
a hunger-weighted version with its own gene. The brain never reads its own
diet: a rabbit's baseline pulls toward `Plant` and a wolf's toward `Meat`, and
selection keeps or changes that. `Eater` opens its seen view so a diet is a
visible fact. `grassPull` and `rabbitPull` give way to the new names, and the
rabbit's baseline is retuned and entry 9's gate rerun, per
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md).

Details to settle in this entry's grill:

- the distance shape. 0023 chose distance to the nearest grass for food and
  summed closeness for other rabbits on purpose: summed closeness to grass
  drifts toward distant grass and can prefer the empty middle between two
  patches, and nearest distance for avoidance pushes a rabbit to the edge of
  its vision. One shape for every fact, or a shape per fact, is open;
- which facts are in the starting set, and which get a hunger-weighted version;
- whether a wolf can catch anything. Every creature decides before anyone
  moves, and a move is a direction and a distance, so a wolf walks toward where
  a rabbit was. The ways out are deciding on each creature's turn, which
  [0005](../decisions/0005-a-day-is-one-method.md) names as a change to one
  method, or living with it, since a wolf only has to end its day on a
  rabbit's tile;
- the acceptance lines: wolves that sustain themselves on rabbits over a seed
  range without wiping them out.

On the page: a wolf with its own sprite that ignores grass and walks toward
rabbits it can see, and a rabbit that disappears when a wolf reaches its tile.

Depends on entry 10. See
[0004](../decisions/0004-creatures-return-intents.md),
[0005](../decisions/0005-a-day-is-one-method.md),
[0013](../decisions/0013-perception-is-one-type.md),
[0016](../decisions/0016-feeding-takes-the-tile.md),
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md),
`docs/ideas/corpses-and-scavenging.md`, `docs/ideas/network-brain.md`.
