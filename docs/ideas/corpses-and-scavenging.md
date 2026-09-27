# Corpses and scavenging

Status: idea.

## What it does

Death leaves a corpse entity on the tile carrying an edible component and a
flammable one, cleared by a system after some days.

## Why it is interesting

Several behaviours arrive for free through rules that already exist. Anything
whose diet accepts corpses can scavenge without a single line about scavenging.
Fire burns bodies, because a corpse is flammable and fire names nothing. A
predator can lose its kill to something faster, which makes speed matter in a
third place. And a starving creature near a battlefield has somewhere to go.

## What it would touch

A corpse species, and whatever kills a creature spawning one, which fits the
rule that whatever kills an entity removes it. Clearing old corpses is one more
world process, alongside regrowth.

## Open questions

Whether a kill also feeds the killer directly, which
[0004](../decisions/0004-creatures-return-intents.md) leaves open.

Whether the corpse's value comes from what the creature was carrying in its
reserve, from its species settings, or both.

How long a corpse lasts, and whether that is a world setting or a property of
the corpse.

Whether killing is worth doing at all once the kill can be stolen, which is the
point and is also how a predator starves.
