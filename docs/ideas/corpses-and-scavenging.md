# Corpses and scavenging

Status: idea.

## What it does

Death leaves a corpse entity on the tile carrying `Meat` and a flammable
component, cleared by a system after some days.

## Why it is interesting

Several behaviours arrive for free through rules that already exist. Anything
whose [diet](diet.md) accepts corpses can scavenge without a single line about
scavenging. Fire burns bodies, because a corpse is flammable and fire names
nothing. A predator can lose its kill to something faster, which makes speed
matter in a third place. And a starving creature near a battlefield has
somewhere to go.

## What it would touch

A corpse species, and whatever kills a creature spawning one, which fits the
rule that whatever kills an entity removes it. Clearing old corpses is one more
world process, alongside regrowth.

## Open questions

Which deaths leave one. A predator kills by eating, per
[0016](../decisions/0016-feeding-takes-the-tile.md), and takes the whole prey,
so a corpse from a kill means the predator eats part of it, while a death by
starving or age leaves the whole body.

Whether the corpse's value comes from what the creature was carrying in its
reserve, from the `Meat` amount its species declares, or both.

How long a corpse lasts. [Lifespan](lifespan.md) would make it a property of
the corpse rather than a world setting.

Whether killing is worth doing at all once the kill can be stolen, which is the
point and is also how a predator starves.
