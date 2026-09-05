# 0005. A day is an ordered list of systems

## Status

Accepted, 2026-09-03.

## Context

Three ways to sequence a day were considered.

An ordered list of systems built in one place makes the order a fact you read in
one constructor.

Systems that register themselves remove that central list, which is attractive
while systems are being added often. It makes order a side effect of
construction sequence, so the answer to "why did fire run before cleanup" stops
being a line you can point at, and two systems added months apart can silently
swap order when construction moves. The central list is one edit per system, paid
rarely, and it is the artefact you read to understand a day.

An event bus lets systems subscribe to events such as an entity dying. Two
subscribers to one event still run in subscription order, so ordering goes
implicit again, and a day becomes a cascade to trace. The fairness guarantee from
[0004](0004-creatures-return-intents.md), that nothing is applied until every
brain has decided, cannot be stated in a pure bus without re-imposing phases on
top of it.

## Decision

The world holds an ordered list of systems, runs each in turn, then increments
the day. Six steps:

1. Decide. Every entity with a brain gets a perception and returns an intent,
   attached to the entity as a component.
2. Resolve. Apply each intent against a list of ids fixed at the start, since
   resolving spawns and removes entities. Movement walks a tile at a time.
   Breeding checks the cooldown and the reserve, spends the cost and spawns a
   child carrying a mutated genome.
3. Feed. Settle contests over edibles and credit metabolisms.
4. Metabolise. Charge the eating cost on the interval, kill anything whose
   reserve falls below zero.
5. World processes, in list order: spawning food, regrowth, fire.
6. Clean up. Despawn the dead, write one census row, detach the intents.

Systems pass work to each other through components rather than through fields on
the world. Step one writes an intent, step two reads it, and a new system that
needs to see intents needs no change anywhere.

The census is the one long-lived exception, since it is durable output rather
than state for the current day. It is what the population graph reads, replacing
the code that counts humans by parsing display strings.

## Consequences

A bus remains available later as a layer inside a single step, if reactive
behaviour is wanted, without disturbing the ordering guarantees.

The order is itself a coupling. Breeding before feeding means a creature cannot
eat and then afford a child on the same day, and that rule lives in the sequence
rather than anywhere a reader would look for it. Changing the order changes
behaviour with no compiler help.

Every day is a full pass over every system, with no notion of a quiet region that
can be skipped.
