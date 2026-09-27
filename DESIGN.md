# HumanSimulation design

The decision log in `docs/decisions/` holds the reasoning behind everything here,
the alternatives that lost, and the concrete type shapes this file leaves out.
Read the relevant entry before writing model code or reopening a design question.

The model layer is being rebuilt, so the tree does not match this file yet. The
work plan under `docs/superpowers/plans/` tracks where that stands.

## Vision

HumanSimulation is a world you watch evolve. Creatures look for food, breed, and
pass varied behaviour to their children, so over a few hundred days the
population drifts toward whatever happens to work.

The fun comes from effects nobody wrote. Fire burns grass, humans and wolves
through one rule, because all three are flammable and the rule names none of
them. A wolf avoids a burning tile because burning is a thing it can see, rather
than because wolves know about fire. Capabilities that meet without having been
introduced are where the world does something you did not plan.

The shape of the code matters as much as the features, because a system that is
pleasant to extend gets extended.

Surprise and breakage look identical from outside, so a run has to be
repeatable. Given the same seed the world replays exactly, so a strange run can
be watched again and taken apart.

## Non-goals

Not an ecology model. Plausible beats accurate.

Not a framework. This is one simulation, and generality no second simulation
needs is cost.

No training. Learning happens through selection across generations, inside the
run you are watching. A brain whose weights are genes is welcome, and one that
must be trained before it is interesting is not. When behaviour looks dull, the
cause is almost always narrow perception and a small action space rather than a
weak learner, so widen the world before climbing the brain ladder.

Single player, local, one window.

Large worlds are not a current target. Grids run to hundreds of tiles and
populations to tens or low hundreds. The architecture keeps the option open
without promising to scale.

## Architecture

### What the world is made of

A world holds a grid, an ordered collection of entities, one random generator and
a day counter, and advances one day at a time.

An entity is an id, a position, a species, a genome and a set of components.
Anything that occupies a tile is an entity: a human, a wolf, a patch of grass, a
rock, a pile of ash. There is no class per kind of thing.

A species is the recipe for a kind of entity. It carries the name, the sprite
that draws it, the gene layout its members inherit, the settings shared by every
member, the brain its members are born with, and the other components they start
with. Every number a rule reads is one of those two: a gene when it varies
between individuals and passes to a child, a setting otherwise. A setting
declares its default and what an editor needs to show it, so the interface can
edit anything without a line of code per number. The brain has its own slot rather than sitting among the components,
because it is the seam that gets swapped. Terrain is a species too, and water
decides nothing, so the brain slot can be empty.

A component is one capability with whatever state it needs. Being edible is a
component. Having a metabolism is a component. Being on fire is a component,
attached when something catches and removed when it burns out. A component that
cuts across kinds is written once, and every kind that carries it gets the
behaviour.

A genome holds what varies between individuals of a species and what changes
between parent and child. The species declares a layout of genes, and each gene
has one of two shapes: a bounded scalar, or a normalised distribution whose own
length can change. The second shape exists because the step distribution already
needs it, and it is also where a network brain's weights would live. Adding an
evolvable trait means adding one entry to a layout.

The grid owns the shape of the world. It holds what occupies each tile and the
scalar fields belonging to the location itself, such as elevation or moisture,
and it is the only thing that knows which directions exist and how far apart two
tiles are. There are four directions today, and because nothing outside the grid
measures distance or enumerates directions, that can change. One question separates them: can
the thing be created and destroyed independently of the tile? Grass, water, rock
and ash all can, so they are entities. A tile always has exactly one elevation
and you cannot detach it, so elevation is a field.

### Replaying a run

The determinism the vision depends on is three invariants rather than a
convention.

The world owns one random generator and hands it to everything that draws from
it. No class builds its own.

Entities are held in ascending id order, every system walks them in that order,
and asking the grid what stands on a tile returns its occupants in that order
too. Any index or spatial structure added later has to preserve it. Resolving is
the one step whose order is a rule rather than the id: intents are applied by
speed descending and equal speeds drawn at random, so acting first is a trait a
creature pays for instead of an accident of when it spawned.

Deciding finishes before anything is applied, so every creature in a day sees
the same world.

Commands from outside queue and take effect at the start of a day, never between
two systems, so a run is described by its seed and the days its commands landed
on.

### Deciding and applying

A brain turns a perception into an intent. A perception is what one creature can
see from where it stands: the tiles within its view range by straight-line
distance, what stands on each, the tags and values those things carry, and the
values its own components publish, such as what is left in its reserve.
Positions in it are relative to the creature, and a tile off the grid is simply
absent, which is how an edge is perceived. An intent is the single action it
wants to take this day, one of moving in a direction for a distance, attacking
something, breeding, or doing nothing.

There is one perception type, and what fills it is a component. A species that
senses differently carries a different builder, and every brain still takes the
same input.

The world resolves intents. A creature says what it wants and the world decides
what actually happens, so the rules about where you may step, what you may
attack and when you may breed live in one place and every creature obeys them
without knowing they exist. Adding water that blocks movement changes the
resolver and nothing else.

A system is a rule the world applies. Hunger charging, grass regrowing and fire
spreading are systems.

The line between a brain and a system is whose rule it is. A system's procedure
belongs to the world and applies identically to everything of its kind, even
when it reads per-individual numbers and even when it rolls dice. A brain's
procedure belongs to the individual: it picks one action out of options that
exclude each other, what governs the pick differs from its neighbour's, and it
passes to its children.

Randomness does not decide this. Fire's ignition roll is still the world's rule.
A brain that picks a direction at random is still a brain, because the weights it
rolls against are its own and its children inherit them.

To make a system's behaviour rich, give its rule better input rather than
promoting it to a brain. Fire that reads wind and moisture from the tile, and
fuel from what it burns, stays one rule that every fire obeys.

One intent per creature per day is the constraint that shapes the rest. A choice
that only becomes available partway through a day cannot be a decision at all,
because the decisions are already collected by then. Anything that wants to be
chosen has to be visible when the day starts, which is why violence is an intent
a creature returns rather than something the feed step does to it.

### The day

A day runs these systems in order:

1. Decide. Every entity with a brain is handed a perception and returns an
   intent. Nothing else changes.
2. Resolve. Each intent is applied against a list of ids fixed at the start,
   since resolving can spawn and remove entities, ordered by the speed gene with
   equal speeds drawn at random. Movement walks one tile at a time and stops where the world says
   it must. Breeding checks the cooldown and the reserve, spends the cost, and
   spawns a child at the parent's position carrying a mutated copy of the
   parent's genome.
3. Feed. In the same order, a creature takes every edible on its tile that its
   diet accepts, crediting its metabolism. A pile taken is gone, so whoever
   comes later finds nothing.
4. Metabolise. Reserves are charged on the eating interval and again for the
   speed the creature carries, and anything that runs out dies.
5. World processes, in list order: spawning new food, regrowth, fire.
6. Clean up. The dead are removed, one census row is written, and the day's
   intents are discarded.

Systems hand work to each other through components rather than through fields on
the world. Step one attaches an intent to the entity and step two reads it, so a
new system that wants to see intents needs no change anywhere. The census is the
one exception, being durable output rather than state for the current day.

### Boundaries

The core knows nothing about the interface, and no simulation rule lives above
the core. A headless run behaves exactly like a watched one.

State leaves as a snapshot of the whole world each day: the day number, the grid,
and for each tile its fields and what stands on it with the tags and values worth
drawing. Commands are the only way in, covering spawning, resetting, editing a
species' settings, and anything else the interface initiates. They queue and
drain at the start of a day, and a paused world drains them at once and draws the
result without advancing. The interface never reaches into the model.

The world advances on the interface's own thread. The snapshot out and the queue
in are the handoff a background thread would need, so moving it off is available
and not needed.

Population counts come from a census the world writes each day.

The core lives in its own package and imports nothing from the interface. A
compile of the core with the interface removed is what enforces it.

A headless runner advances a seeded world for a given number of days and reports
population and gene means. It is the only thing that catches a build which
compiles, passes every test, and quietly stops selecting for anything.

### Starting a world

A world is built from its dimensions, a seed, the set of species it knows, and an
initial population given as counts or positions per species. Everything else
follows from advancing days. The same construction serves the interface and the
headless runner, which is what makes a watched run and a scripted one comparable.

### Keeping large worlds open

No system iterates the entity set or the grid directly. Every traversal goes
through a query on the world or the grid, so an index, a spatial structure or a
partial snapshot can be added later without a caller noticing.

Two costs are known and unfixed: ground cover as entities means one entity per
tile across a covered world, and a day is a full pass with no notion of a quiet
region to skip.

### The claim

Adding a capability costs one component, plus either a new system or a branch in
an existing one, and changes nothing else. The first time something forces a
change to the world, the entity, the genome or the shape of a day, the design has
a flaw and the work plan should record it.

A wolf tests it. The wolf is a species value and no existing file changes,
because the world has never needed to know what a human is. What the species
value does not give you is the brain: a wolf that hunts has to condition on where
prey is, which a brain that picks a weighted-random direction cannot do. The data
is free and the brain is the work.
