# Lifespan

Status: planned, roadmap entry 8.

## What it does

A component that counts its carrier's days and ends the carrier when they run
out, the way `Metabolism` ends a carrier whose reserve falls below zero. A
species that declares it sets how long its members last with a setting of its
own. Grass declares it, so grass left uneaten withers. Rabbits declare it too,
with a default long enough that it rarely ends one, and the part reports each
rabbit's age.

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
selection gate and its tuned food defaults, since withering changes how much
grass the world holds. The census or a snapshot gains age if runs are to
measure it.

## Open questions

Whether a lifespan is a fixed number of days or a daily chance, which spreads
deaths out and avoids a whole drop of grass vanishing on one day.

The rabbits' default, and what runs would justify lowering it.

Whether a rabbit's lifespan later becomes a gene, which needs a tradeoff to
stop it pinning to its upper bound, per
[gene tradeoffs](gene-tradeoffs.md).

Whether a creature that dies of age leaves a corpse, per
[corpses and scavenging](corpses-and-scavenging.md).
