# 0024. A part can end its carrier

## Status

Accepted, 2026-10-26.

## Context

Starving was the only way an entity ended other than being eaten, and the day's
fourth step asked `Metabolism` for it by name. Grass waited until it was eaten,
so a population ate every drop however well it foraged, and survival could not
see foraging skill, per entry 7's finding. Grass that withers needs a second
way to end, and a corpse that rots or a fire that burns out would each add
another.

A contract only for parts that can end their carrier was the first shape. It
generalises the step halfway: a part that changes only itself each day and
never ends anything, such as a fire running down its fuel, would need a contract
of its own or a step of its own.

Letting entities apply their whole day themselves was also weighed, so that a
creature that stands on grass decides to eat it and then checks its own reserve.
That is the 2022 model, and the reasons it lost still hold. Two rabbits on one
grass would be settled by whichever is updated first, which is id order, where
[0014](0014-resolve-order-comes-from-a-speed-gene.md) makes it the speed gene.
Legality would spread into every creature type, per
[0004](0004-creatures-return-intents.md). And one creature acting early would
change what a later one sees on the same day, per
[0005](0005-a-day-is-one-method.md). A rule touching two entities or the grid
stays the world's.

A lifespan could be a daily chance of ending, which spreads deaths out and
avoids a whole drop withering on one day. Lifetimes would then carry no age,
the rule would draw from the shared generator for every carrier every day, and
a test of it would need dice. A drop's leftovers vanishing together is also the
sign on the page that food was lost.

Rabbits could declare a lifespan now, with a default long enough that it rarely
ends one, so their age became visible. Nothing reports how long rabbits live,
so the default would be a guess, and age needs a census field and a measure of
its own. That half waits in `docs/ideas/lifespan.md`.

## Decision

Every part may update itself once a day. `Component` has a `tick` that does
nothing by default, sees only the part's own state and the values its `Spawn`
handed it, and returns the cause its carrier ends with, or nothing.

```java
interface Component {
    Class<? extends Component> key();
    Record ownView();
    Optional<Record> seenView();

    default Optional<String> tick() {
        return Optional.empty();
    }
}
```

The day's third step, Tick, walks every entity in id order and ticks each of
its parts. The first part to return a cause ends its carrier, which is removed
at once, per [0005](0005-a-day-is-one-method.md), and its later parts are not
ticked.

`Metabolism` ticks by charging its reserve on the eating interval, and ends its
carrier as `starved` when the reserve falls below zero. `Lifespan` counts its
carrier's days and ends it as `aged` on the day they run out. Its length is a
setting of the declaring species, `lifespan`, per
[0017](0017-every-number-is-a-setting-or-a-gene.md). Grass declares it, so a
grass dropped at the end of one day can be eaten on each of the next `lifespan`
days and withers in the Tick of the last. Rabbits do not declare it.

The feed rule ends what it eats as `eaten`. Every removal names its cause, and
the census counts them, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md).

`Lifespan`'s own view reports its age and the days left. No brain reads it yet.

The brain has its own slot rather than sitting among the parts, per
[0003](0003-entities-carry-components.md), so Tick does not tick it. A brain
holds no state that changes by the day.

## Consequences

Adding a way to end, or a part that changes only itself, costs one component and
no edit to the day.

A cause is a string, so a typo shows up as a new cause in the census rather
than a compile error, as setting keys already do.

Parts tick in the order the species lists them, so a carrier that starves and
ages on the same day is counted under whichever part comes first.

A part's tick cannot see the world, so anything that needs a neighbour or a
tile, such as fire spreading, stays a world process.

Grass that withers opens the energy budget: food a population fails to find is
lost, so a better forager feeds more of its kind. How much withers depends on
the lifespan and the food defaults together, and entry 8 tunes them as a pair.
