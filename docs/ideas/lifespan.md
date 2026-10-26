# Lifespan

Status: planned, roadmap entry 8.

## What it does

A component that counts its carrier's days and ends the carrier when they run
out, the way `Metabolism` ends a carrier whose reserve falls below zero. A
species that declares it sets how long its members last with a setting of its
own. Grass declares it, so grass left uneaten withers. Rabbits could declare it
too, with a default long enough that it rarely ends one, so their age becomes
visible.

Entry 8 builds the grass half, per
[0024](../decisions/0024-a-part-can-end-its-carrier.md): a fixed number of days,
grass only. The rabbit half stays open here.

## Why it is interesting

Grass that waits forever closes the energy budget. A population eventually
eats every drop however well it forages, so its size is the energy coming in
divided by what each member burns, and foraging skill cannot change it. Entry
7's tuning log found this at run 10: survival in separate worlds could not see
foraging skill. Grass that withers makes food a population fails to find a
loss, so a better forager feeds more rabbits.

Nothing in the documents decides whether a fed creature can live forever. It
was never asked: starvation arrived in entry 5 as the first thing that removes
a creature, and nothing since has added a second. A rabbit with a large reserve
keeps its genome in the population and keeps eating. Whether that happens often
is unknown, since nothing reports a rabbit's age. Declaring the part on rabbits
with a long default makes age visible, and runs then show whether the default
should come down.

The same part ends a corpse after some days, a fire that burns out, or water
that dries up, so the world never needs a system that knows which entity
expires.

## What it would touch

A new component, and the day's step that removes the dead asks every part that
can end its carrier rather than asking `Metabolism` by name. A setting per
declaring species, per
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md). Entry 7's
tuned food defaults and entry 9's selection gate, since withering changes how much
grass the world holds. The census or a snapshot gains age if runs are to
measure it.

## Open questions

Whether rabbits declare it, with what default, and what runs would justify
lowering it. Making their age visible needs an age field in the census and a
measure in `compare.py`.

Whether a rabbit's lifespan later becomes a gene, which needs a tradeoff to
stop it pinning to its upper bound, per
[gene tradeoffs](gene-tradeoffs.md).

Whether a creature that dies of age leaves a corpse, per
[corpses and scavenging](corpses-and-scavenging.md).
