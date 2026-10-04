# 0005. A day is one method, and world processes are a list

## Status

Accepted, 2026-09-03. Revised 2026-10-04.

## Context

The steps of a day hand data to each other. Decide produces one intent per
creature, and resolve and feed both need those intents and the day's order from
[0014](0014-resolve-order-comes-from-a-speed-gene.md). Where that data lives
between steps is part of how a day is sequenced.

An ordered list of generic systems, built in one place, was the first shape.
Generic systems can only pass data through something they all share, so the
intents were attached to the entities and detached at clean up. That puts
state on an entity which belongs to the day rather than to the entity, and has
one step writing it for another step to read. A per-day object handed to every
system avoids that and adds a type whose only job is carrying two lists. Either
way the benefit was that a new system could read intents with no change
anywhere, and no system needs that yet.

Deciding each creature on its turn, against a frozen copy of the world taken at
the start of the day, gives the same outcomes as deciding everything first. It
needs a second copy of the world, since the snapshot in
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md) holds display values
rather than the component views a perception is built from. Deciding everything
first gets the frozen world for free, because nothing moves while the decide
loop runs.

Systems that register themselves make order a side effect of construction
sequence, so the answer to "why did fire run before clean up" stops being a
line you can point at. An event bus makes a day a cascade to trace, and the
guarantee that nothing is applied until every brain has decided cannot be
stated in a pure bus without re-imposing phases on top of it.

Taking the census and the snapshot before advancing the counter was the first
order. The first completed day then reported day 0, the number shown was the
day about to run, and a snapshot taken while paused would disagree with the
last report.

## Decision

A day is one method on the world. The fixed steps are ordinary calls in that
method, and the data between them is local variables. Only the world processes
are a list, since that is where new systems get added.

```java
DayReport advance() {
    drainCommands();
    List<Decision> decisions = decide();
    List<Decision> order = speedOrder(decisions);
    resolve(order);
    feed(order);
    metabolise();
    for (WorldProcess p : processes) p.run(this);
    day++;
    CensusRow census = takeCensus();
    WorldSnapshot snapshot = snapshot();
    return new DayReport(snapshot, census);
}
```

Commands drain first, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md). Then:

1. Decide. Every entity with a brain gets a perception and returns an intent,
   collected as a list of decisions. Nothing in the world changes.
2. Resolve. Apply each decision in the day's order, under the legality rules in
   [0004](0004-creatures-return-intents.md). Movement walks a tile at a time.
   Breeding checks the interval and the reserve, spends the cost and spawns a
   child carrying a mutated genome and an endowment.
3. Feed. In the same order, each creature takes every edible on its tile, per
   [0016](0016-feeding-takes-the-tile.md).
4. Metabolise. Charge the eating cost on the interval and the cost of speed.
   Anything whose reserve falls below zero dies.
5. World processes, in list order: spawning food, regrowth, fire.
6. Take one census row, and return it with the snapshot.

The day counter counts days completed, so it rises before the census and the
snapshot are taken and both carry the number of the day just run. A new world
is on day 0, and its first `advance()` reports day 1, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md).

Whatever kills an entity removes it at once, in whichever step that happens.

An entity holds only its own state. Intents and the day's order never touch it,
and they are gone when the method returns.

The census row is the one output that outlives the day. The report carries it
out with the snapshot, and whoever reads it keeps the history, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md). It is what the
population graph reads, replacing the code that counts humans by parsing
display strings.

## Consequences

The order of a day is read top to bottom in one method.

A new system that wants the day's intents takes them as an argument, which is
one edit in the method you are already editing to add it.

The order is itself a coupling. Breeding before feeding means a creature cannot
eat and then afford a child on the same day, and that rule lives in the sequence
rather than anywhere a reader would look for it. Changing the order changes
behaviour with no compiler help.

A creature that starves in step four is already gone in step five, so nothing in
the world processes can see or burn it.

Every day is a full pass over every step, with no notion of a quiet region that
can be skipped.

If same-day reactions turn out to matter, such as a rabbit fleeing a wolf that
moved next to it earlier in the day, deciding on each creature's turn is a
change to this one method.
