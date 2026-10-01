# Diet

Status: idea.

## What it does

Lets a creature say which edibles it accepts, so the feed step stops taking
every edible on the tile. Until then every eater eats everything edible, per
[0016](../decisions/0016-feeding-takes-the-tile.md).

## Why it is interesting

The first predator needs it, since a wolf that eats grass is not a predator.
Done the right way, it is also a place where capabilities meet without being
introduced: a new plant gets eaten by every herbivore without anyone listing it.

## What it would touch

`Edible` and `Metabolism`, and the feed step's filter. Three shapes were
considered.

A diet lists the species a creature eats. It is the simplest, and every new
plant means editing every herbivore, and the wolf's diet names the rabbit, which
the vision in `DESIGN.md` argues against.

`Edible` carries a food kind such as plant or meat, and `Metabolism` carries the
kinds its carrier accepts. Grass is a plant and a corpse is meat. This is the
recommended one, at the cost of a small fixed list of kinds.

No diet at all, which is where the rebuild starts.

## Open questions

Whether the accepted kinds become a gene, so a diet can evolve.

Whether it lands with the predator in roadmap entry 6 or before it.
