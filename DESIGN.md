# HumanSimulation design

The decision log in `docs/decisions/` holds the reasoning behind everything
here, the alternatives that lost, and the concrete type shapes this file leaves
out. Read the relevant entry before writing model code or reopening a design
question.

The model layer is being rebuilt, so the tree does not match this file yet.
`docs/roadmap.md` says which parts exist and what comes next.

## Vision

HumanSimulation is a world you watch evolve. Creatures look for food, breed, and
pass varied behaviour to their children, so over a few hundred days the
population drifts toward whatever happens to work.

The fun comes from effects nobody wrote. Fire burns grass, rabbits and wolves
through one rule, because all three are flammable and the rule names none of
them. A wolf avoids a burning tile because burning is a thing it can see, rather
than because wolves know about fire. Capabilities that meet without having been
introduced are where the world does something you did not plan.

The shape of the code matters as much as the features, because a system that is
pleasant to extend gets extended.

Surprise and breakage look identical from outside, so a run has to be
repeatable. Given the same seed and the same commands the world replays exactly,
so a strange run can be watched again and taken apart.

## Goals

Two goals decide what gets built.

It is pretty and satisfying to watch, so that people open it for the sake of
watching. Creatures move smoothly between tiles rather than jumping, and the
interface is a web page anyone can reach from a link once it is hosted. A
feature that makes the world more interesting and harder to read on screen is
not finished.

An AI agent can experiment with it easily. An agent starts worlds, drives them,
runs many seeded runs and reads the results as data, with nobody at the screen.
That is how new features and new brains get tested.

Further out, a creature can start from a fitted brain that evolution then
refines, and a brain worth keeping can be saved and loaded into another run.
`docs/ideas/brain-library.md` holds that.

## Non-goals

Not an ecology model. Plausible beats accurate.

Not a framework. This is one simulation, and generality no second simulation
needs is cost.

No training. Learning happens through selection across generations, inside the
run you are watching. A brain whose weights are genes is welcome, and one that
must be trained before it is interesting is not. Fitting a starting brain once,
to imitate one that already works, is a warm start rather than training, since
selection takes over from there. When behaviour looks dull, the
cause is almost always narrow perception and a small action space rather than a
weak learner, so widen the world before climbing the brain ladder.

Single player. Each viewer watches their own world in a browser tab, served from
their own machine or from a host. There are no shared worlds and no accounts.

Large worlds are not a current target. Grids run to hundreds of tiles and
populations to tens or low hundreds. The architecture keeps the option open
without promising to scale.

## Architecture

### How the code is built

The core is a domain model in the sense of domain-driven design. The words in
this file are its ubiquitous language, the world is the aggregate root, entities
are known by their id, and positions, genomes, intents and perceptions are
immutable values. Behaviour lives in the rules of a day rather than on a class
per kind of creature.

The architecture is hexagonal. The domain depends on nothing but itself and the
Java base library. Commands are the one way in, snapshots and the census the way
out, and the web interface and the command line are adapters around it.

Collaborators are injected by constructor, and `Main` wires everything by hand.

Development is test-driven. A test is written and seen to fail before the code
that makes it pass.

Values are valid by construction. A value checks itself when it is built and
throws if it is wrong, nothing is ever null, and absence is an empty optional
or collection. Every compiler warning fails the build.

### What the world is made of

A world holds a grid, an ordered collection of entities, one random generator
and a day counter, and advances one day at a time.

An entity is an id, a position, a species, a genome and a set of components.
Anything that occupies a tile is an entity: a rabbit, a wolf, a patch of grass,
a rock, a pile of ash. There is no class per kind of thing.

A species is the recipe for a kind of entity. It carries the name, the sprite
that draws it, the gene layout its members inherit and the baseline genome the
first generation starts from, the settings shared by every member, the brain its
members are born with, and the other components they start with. Every number a
rule reads is declared on its species or on the world: a gene when it varies
between individuals and passes to a child, a setting otherwise, and a gene's own
bounds and mutation size in the layout. A setting declares its default and what
an editor needs to show it, so the interface can edit anything without a line of
code per number. The brain has its own slot rather than sitting among the
components, because it is the seam that gets swapped. Terrain is a species too,
and water decides nothing, so the brain slot can be empty.

A component is one capability with whatever state it needs. Being edible is a
component. Having a metabolism is a component. Being on fire is a component,
attached when something catches and removed when it burns out. A component that
cuts across kinds is written once, and every kind that carries it gets the
behaviour.

A genome holds what varies between individuals of a species and what changes
between parent and child. The species declares a layout of genes, and each gene
has one of two shapes: a bounded scalar, or a normalised distribution whose own
length can change. The second shape exists because the step distribution already
needs it. A third shape, for a network brain's weights, arrives with that brain.
Adding an evolvable trait means adding one entry to a layout. The first
generation starts from the species' baseline genome with mutation applied, so a
new species begins with working behaviour rather than random values.

The first brain is a fixed set of rules whose parameters are genes. A network
brain, started from weights that imitate it, comes when those rules run out.

The grid owns the shape of the world. It holds what occupies each tile and the
scalar fields belonging to the location itself, such as elevation or moisture,
and it is the only thing that knows which directions exist and how far apart two
tiles are. There are four directions today, and because nothing outside the grid
measures distance or enumerates directions, that can change. One question
separates an entity from a field: can the thing be created and destroyed
independently of the tile? Grass, water, rock and ash all can, so they are
entities. A tile always has exactly one elevation and you cannot detach it, so
elevation is a field.

### Replaying a run

The determinism the vision depends on is four invariants rather than a
convention.

The world owns one random generator and hands it to everything that draws from
it. No class builds its own.

Entities are held in ascending id order, every system walks them in that order,
and asking the grid what stands on a tile returns its occupants in that order
too. Any index or spatial structure added later has to preserve it. Resolving
and feeding are the steps whose order is a rule rather than the id: they walk
one order per day, speed descending with equal speeds drawn at random, so acting
and eating first is a trait a creature pays for instead of an accident of when
it spawned. Nothing loops over a collection whose order is undefined, such as a
hash map, so two processes given the same seed agree as well as two worlds in
one.

Deciding finishes before anything is applied, so every creature in a day sees
the same world.

Commands from outside queue and take effect at the start of a day, or at once
when a paused adapter asks for them, never between two systems, so a run is
described by its seed and its command log.

### Deciding and applying

A brain turns a perception and its options into an intent. A perception is what
one creature can see from where it stands: the tiles within its view range by
straight-line distance, what stands on each, and the components of its own and
of those things. A brain asks for components by class, so it checks for
something edible rather than for a string that says so. Each component declares
what its carrier sees of it and what others in range see, which may be nothing,
so a creature can read its own reserve while its neighbours cannot, and what one
creature can know about another is a deliberate choice. Positions in it are
relative to the creature, and a tile off the grid is simply absent, which is how
an edge is perceived. An intent is the single action it wants to take this day,
one of moving in a direction for a distance, breeding, or doing nothing. There
is no attack until a predator needs one.

There is one perception type, and what fills it is a component. A species that
senses differently carries a different sense, the component that builds its
perception, and every brain still takes the same input.

The options are what the world offers a creature when the day starts: the
directions the grid has, and later whatever else it can choose between. They
come from the grid and the creature's components rather than from its sense, so
a new sense never has to supply them. An option is not a promise, since the
resolver still decides what happens.

The world resolves intents. A creature says what it wants and the world decides
what actually happens, so the rules about where you may step and when you may
breed live in one place and every creature obeys them without knowing they
exist. Adding water that blocks movement changes the resolver and nothing else.

Whatever kills an entity removes it at once, so there is never a dead thing in
the world for a later system to skip. Every loop walks a copy of the ids it
started with and passes over any that no longer resolve.

A system is a rule the world applies. Hunger charging, grass regrowing and fire
spreading are systems.

The line between a brain and a system is whose rule it is. A system's procedure
belongs to the world and applies identically to everything of its kind, even
when it reads per-individual numbers and even when it rolls dice. A brain's
procedure belongs to the individual: it picks one action out of options that
exclude each other, what governs the pick differs from its neighbour's, and it
passes to its children.

Randomness does not decide this. Fire's ignition roll is still the world's rule.
A brain that picks a direction at random is still a brain, because the weights
it rolls against are its own and its children inherit them.

To make a system's behaviour rich, give its rule better input rather than
promoting it to a brain. Fire that reads wind and moisture from the tile, and
fuel from what it burns, stays one rule that every fire obeys.

One intent per creature per day is the constraint that shapes the rest. A choice
that only becomes available partway through a day cannot be a decision at all,
because the decisions are already collected by then. Anything that wants to be
chosen has to be visible when the day starts, which is why violence, when it
arrives, is an intent a creature returns rather than something the feed step
does to it.

### The day

A day runs these steps in order:

1. Decide. Every entity with a brain is handed a perception and its options,
   and returns an intent. Nothing else changes.
2. Resolve. Each intent is applied against a list of ids fixed at the start,
   since resolving can spawn and remove entities, ordered by the speed gene with
   equal speeds drawn at random. Movement walks one tile at a time and stops
   where the world says it must. Breeding checks the interval and the reserve,
   spends the cost, and spawns a child at the parent's position carrying a
   mutated copy of the parent's genome and part of the cost as its reserve, its
   endowment. A child is not on the day's list, so it first acts and feeds the
   next day.
3. Feed. In the same order, a creature takes every edible on its tile,
   crediting its metabolism. A pile taken is gone, so whoever comes later finds
   nothing.
4. Metabolise. Reserves are charged on the eating interval and again for the
   speed the creature carries, and anything whose reserve falls below zero dies.
5. World processes, in list order: spawning new food, regrowth, fire.
6. Clean up. One census row is produced.

A day is one method on the world, and the steps are calls in it. The intents
and the day's order pass between them as local variables, so an entity holds
only its own state and nothing from the day outlives it. Only the world
processes are a list, since that is where new systems get added. The method
returns the day's report, which holds its snapshot and census row.

### Boundaries

The core knows nothing about the interface, and no simulation rule lives above
the core. A run with no browser behaves exactly like a watched one.

State leaves as a snapshot of the whole world each day: the seed, the day
number, the grid, and for each tile its fields and what stands on it with the
values worth drawing. Commands are the only way in, covering spawning,
resetting, editing a species' settings, and anything else the interface
initiates. They queue and drain at the start of a day. The interface never
reaches into the model. A command is checked before it changes anything, and a
bad one changes nothing.

The day number counts days completed, so a new world is on day 0. A reset with
a seed leaves the world exactly as building it with that seed would: empty, and
on day 0.

Pausing is the adapter's business rather than the world's. A paused adapter
asks the world to apply what is pending, which drains the queue and returns a
snapshot without advancing the day.

Each world is owned by one thread, and everything that touches it, submitting a
command included, is handed to that thread as a task. The world itself is not
thread-safe and does not need to be.

Population counts come from a census row the world produces each day, one row
per day with no gaps. The history of rows belongs to whoever reads them, so
resetting the statistics is something an adapter does to its own history and
not a command. A world reset starts the history afresh, since its days start
again at 0.

The core lives in the package `io.github.decoppelelias.humansimulation.domain`
and depends only on itself and `java.base`. A test runs `jdeps` over the
compiled classes and fails on anything else. The package is flat. The world,
what builds it and the values crossing the boundary are public and the rest is
package-private, so nothing outside it can reach past the world.

The web adapter serves the interface and an HTTP API, and holds worlds by an id
nobody can guess. The browser draws what the snapshots say and animates between
them. An agent drives worlds over the same API with no browser open.

A `run` command advances a seeded world for a given number of days and prints
the census as JSON Lines, with population and gene means. It is the only thing
that catches a build which compiles, passes every test, and quietly stops
selecting for anything.

### Starting a world

A world is built from its width and height, a seed and the set of species it
knows, and starts empty. Its starting population is spawn commands, which
whoever creates the world sends on day 0 and again after a reset, so the seed
and the command log describe a run from its first creature. A spawn names its
species, and each member is drawn from its species' baseline genome. Spawning
works on any day, which is how creatures are added to a running world.
Everything else follows from advancing days. The same construction serves the
web interface and the `run` command, which is what makes a watched run and a
scripted one comparable.

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
change to the world, the entity, the genome or the shape of a day, the design
has a flaw, and the decision entry for the topic it breaks records it.

The rabbit is the first species, and a wolf tests it. The wolf is a species
value, plus one intent case and its branch in the resolver once it hunts, and
nothing else changes, because the world has never needed to know what a rabbit
is. What the species value does not give you is the brain: a wolf that hunts has
to condition on where prey is, which a brain that picks a weighted-random
direction cannot do. The data is free and the brain is the work.
