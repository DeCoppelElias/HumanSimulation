# 10. Diet

Status: not started.

A creature says which edibles it accepts, and the feed step takes only those,
so the wolf in entry 11 arrives already ignoring grass. Until now every eater
takes every edible on its tile, which this entry changes in
[0016](../decisions/0016-feeding-takes-the-tile.md).

Details to settle in this entry's grill: which of the three shapes in
`docs/ideas/diet.md` to build, where it recommends a food kind on `Edible` and
the kinds a `Metabolism` accepts; what a wolf accepts, given that a kill leaves
no remains in entry 11; and whether the accepted kinds become a gene.

Acceptance: an eater that does not accept grass leaves it on its tile, and a
rabbit run on a given seed reports the same census as before the change.

On the page: nothing changes, since rabbits are the only eaters and still eat
grass. Entry 11 makes it visible, per
[0002](../decisions/0002-replace-the-model-layer-in-place.md).

Depends on entry 9. See
[0016](../decisions/0016-feeding-takes-the-tile.md),
`docs/ideas/diet.md`.
