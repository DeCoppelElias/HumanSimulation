# Ideas

One file per idea that is wanted and not decided. Each has a status line, then
what it does, why it is interesting, what it would touch, and what is still
open. Named rather than numbered, because ideas get promoted and dropped and
their order means nothing.

This is not a roadmap. Nothing here is committed to.

An idea that gets adopted is settled in `docs/decisions/`, in the entry for its
topic or in a new one if the topic is new, and goes onto `docs/roadmap/`. It
keeps its file, marked `planned`, and once built its status line links the entry
that settled it.

- [Aggression at contested food](aggression-at-contested-food.md) the 2022 food
  contest, left out of the rebuild on purpose.
- [Ancestor competition](ancestor-competition.md) evolved genomes against the
  ones they started from, in one world and apart, so the world judges.
- [Bounded census history](bounded-census-history.md) keeping a playing world's
  history from growing without limit.
- [Brain library](brain-library.md) saving genomes you like and loading them
  into a later run.
- [Brain memory](brain-memory.md) a brain that keeps a chosen target from one
  day to the next.
- [Browser smoke test](browser-smoke-test.md) an agent checks what the page
  actually renders.
- [Comparison statistics](comparison-statistics.md) an effect size and a
  test in `compare.py`, over more seeds.
- [Control species](control-species.md) a species that never learns, run
  beside the rabbits to show what selection adds.
- [Corpses and scavenging](corpses-and-scavenging.md) death leaves something
  edible behind.
- [Diet](diet.md) which edibles a creature accepts, needed once a predator
  exists.
- [Domain subpackages](domain-subpackages.md) splitting the flat domain
  package by topic once it grows hard to navigate.
- [Drawing a stacked tile](drawing-a-stacked-tile.md) a way to show every
  occupant of a shared tile.
- [Error Prone](error-prone.md) failing the build on bug patterns javac does
  not warn about.
- [Founder benchmark](founder-benchmark.md) scoring one genome by the
  population it founds, which is how brains get compared.
- [Gene tradeoffs](gene-tradeoffs.md) a cost of its own for each gene that
  only helps, instead of a charge against the reserve.
- [Golden-run test](golden-run-test.md) pinning a seeded run's output across
  builds, left out for its overhead.
- [Hexagonal grid](hexagonal-grid.md) six equidistant neighbours, so sight and
  movement finally agree.
- [Humans](humans.md) the species the project is named for, after rabbits and
  wolves.
- [Immigration](immigration.md) creatures of a species arriving every few
  days, the way food does.
- [Javadoc on the domain](javadoc-on-the-domain.md) short Javadoc on public
  domain types and decision numbers in comments.
- [Measuring test quality](measuring-test-quality.md) coverage reports, a
  coverage threshold, or mutation testing.
- [Lifespan](lifespan.md) a part that ends its carrier after some days, so
  grass withers and rabbits age.
- [MCP server](mcp-server.md) an agent drives a world through typed tools
  over stdio.
- [Mutation shadow](mutation-shadow.md) genomes changed by mutation alone,
  to tell selection from drift.
- [Network brain](network-brain.md) the second brain, warm-started from the
  fixed rules.
- [Patchy food](patchy-food.md) food arriving near food, so herding has
  something to gain.
- [Property-based tests](property-based-tests.md) jqwik for the domain's
  invariants, with shrunk counterexamples.
- [Remove command](remove-command.md) taking an entity out of a running world
  by id.
- [Run comparison](run-comparison.md) one shared way for an agent to tell
  whether a variant did better.
- [Run settings and genes file](run-settings-file.md) many `run` overrides,
  of settings or starting genes, kept in one file.
- [Stomach capacity](stomach-capacity.md) a limit on what a creature can hold,
  which makes how much to eat a decision.
- [World persistence](world-persistence.md) a server's worlds surviving a
  restart.
- [World size on the page](world-size-on-the-page.md) choosing a world's width
  and height on the create screen.
