# 10. Diet

Status: done.

A creature says which foods it takes, and the feed step takes only those, so
the wolf in entry 11 arrives already ignoring grass, per
[0016](../decisions/0016-feeding-takes-the-tile.md). `Edible` becomes an
interface, grass carries a `Plant` component that implements it, and a rabbit
carries `Eater` taking `Plant`, fixed in its species recipe. `Eater` shows
nothing to others. The scoring brain counts a tile as food when something there
carries `Plant`, which entry 11 replaces with a pull per visible fact.

`Meat`, a rabbit carrying it, and an `Eater` that takes it wait for the wolf in
entry 11, since nothing would eat meat before then.

Acceptance:

- an eater whose `Eater` does not take `Plant` leaves grass on its tile, and a
  rabbit on the same tile still eats it;
- a creature with a `Metabolism` and no `Eater` eats nothing;
- the scoring brain is pulled by `Plant`, tested with a perception and no world;
- `run --seed 42 --days 1000` and one more seed print the same output before and
  after the change, a one-off diff logged in the plan.

On the page: nothing changes, since rabbits are the only eaters and still eat
grass. Entry 11 makes it visible, per
[0002](../decisions/0002-replace-the-model-layer-in-place.md).

Depends on entry 9. See
[0013](../decisions/0013-perception-is-one-type.md),
[0016](../decisions/0016-feeding-takes-the-tile.md),
`docs/ideas/diet.md`, `docs/ideas/component-interactions.md`.
