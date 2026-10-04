# AGENTS.md

Java Swing application that simulates humans searching for food on a 2D grid.
It was written in September 2022. README.md describes what the application does
and how to use it.

## Where to find things

`DESIGN.md` describes the system as it is meant to be: what it is for, what it is
not, and how the model fits together. Read it before changing anything in the
model layer, and before proposing a feature.

`docs/decisions/` is the decision log, one entry per topic with what was
rejected and why. Read the entry before reopening a design question. When a
choice changes, rewrite its entry in place and update `DESIGN.md` to match. Add
an entry only for a new topic. `docs/decisions/README.md` indexes them.

`docs/ideas/` holds what is wanted and not decided, one file per idea. Look there
before proposing a feature, and put a rejected or deferred one there rather than
losing it. It is not a roadmap.

`docs/roadmap.md` is the committed build order for the model layer: what is
planned, in what sequence, and whether each entry is done yet. Read it for
current status and what comes next. The model layer is being rebuilt, so the
tree does not match `DESIGN.md` yet.

The work plan under `docs/superpowers/plans/` tracks finer-grained work in
progress. It is gitignored and may be missing from a fresh clone.

Keep this file mechanical. Anything about why the system is shaped the way it is
belongs in `DESIGN.md` or the decision log.

## Workflows

Project skills live in `.claude/skills/`, one folder each with a `SKILL.md`.
An agent that does not load skills can read them directly.

- `changing-decisions` for any change to a settled design choice, or a new one.
- `promoting-ideas` to move an idea onto the roadmap.
- `implementing-roadmap-entries` to start, resume or finish a roadmap entry.
- `auditing-docs` to check the documents against each other.
- `grilling` to settle what an entry or idea leaves open, one question at a
  time, before planning it.
- `gui-smoke-test` to verify the Swing app, see below.

A new idea goes in `docs/ideas/` without asking, in the format its README gives.
Ask Elias before promoting an idea, changing an accepted decision, touching the
roadmap's order, or pushing, merging or tagging. `docs/decisions/0019` says why.

Each workflow skill has an `evals/` scenario. After editing a skill, run its
scenario on a fresh agent and check the result against the pass criteria there.

    python tools/docs-check.py         links, indexes and status lines
    python tools/test_docs_check.py    its tests

The pre-commit hook and CI run `docs-check.py`.

## Branches

- A roadmap entry is built on its own branch off `master`, named
  `entry-N-slug`, such as `entry-1-minimal-loop`.
- Other work, such as a docs change, gets a short branch named for what it
  does, such as `docs/standards-grill`.
- When the work is finished and reviewed and Elias says go, merge it into
  `master` locally, push `master`, and delete the branch. There are no pull
  requests. CI runs on the push.
- `master` always holds finished work, never half an entry.

`docs/decisions/0002` says why.

## Layout

- `src/main/java/` `Main` and `Application` at the root, then
  `SimulationApplication/` (the model), `GuiPackage/` (the Swing UI, with
  `GuiController` between the panels and the model), and `DataAnalytics/`
  (population counts and the JFreeChart line chart).
- `src/test/java/Test/` JUnit tests.
- `src/main/resources/` `Human.png` and `Food.jpg`, loaded with
  `getResource("/Human.png")`, so they have to stay at the classpath root.

## Build and run

    ./mvnw verify     compile, check formatting, run tests
    ./mvnw package    also build target/HumanSimulation.jar
    java -jar target/HumanSimulation.jar

Java 21. No `mvn` on PATH is needed, the wrapper fetches it. `jfreechart:1.5.3`
and `junit-jupiter:6.1.3` are pinned in `pom.xml`, and the enforcer plugin fails
the build on a version range or a snapshot. Let Dependabot propose upgrades.

CI runs `./mvnw -B verify` on Linux and Windows, on pushes to `master` and on
pull requests. A `v*` tag builds the jar and attaches it to a GitHub Release,
taking the version from the tag, so the pom stays on `-SNAPSHOT` between
releases and never needs a manual bump. The jar is byte-reproducible.

## Comments

Three rules. A comment describes the current state, never how the code got
there. It exists only if it says something the code does not already say. It
stays short.

So no changelog in comments, no "used to", no restating the line below, no
commented-out code, and no long block where a clause would do.

    python tools/comment-check.py --staged

Enable it as a pre-commit hook with `git config core.hooksPath .githooks`, once
per clone. It checks staged files only, so 2022 comments are left alone until
you edit that file.

## Formatting

Spotless with palantir-java-format, ratcheted against `origin/master`, so only
files you actually change get formatted. Run `./mvnw spotless:apply` on your own
changes.

Never format the whole tree. That would rewrite every Java file and move
`git blame` off the 2022 commits, which is what the ratchet exists to prevent.

`ResourceLoadingTest` guards the packaging contract, not the simulation: the
icons have to stay directly under `src/main/resources` or `GridPanel` fails at
runtime while everything still compiles.

## Before writing tests

Write the test first, run it, and see it fail for the reason it names before
writing the code that makes it pass. `docs/decisions/0012` says why.

`GridWorld` owns the one `RandomGenerator` every behaviour class draws from;
construct it via `new GridWorld(width, height, new Random(seed))` for a
reproducible run, or use `TestWorlds.seeded` in `src/test/java/Test/`.
`DeterminismTest` guards that a seeded run replays exactly. Build test humans
through `TestWorlds` rather than by hand: each `Human` constructor calls
`movementBehaviour.setHuman(this)`, so two humans sharing one
`MovementBehaviour` or `FoodBehaviour` instance end up pointing at each
other's state.

`v1.0-original-2022` and the `original-2022` branch hold the 2022 version, with
the old IntelliJ files, the flat `src/` layout and a committed jar. Both are
immutable. Land changes on top of `master`.

## GUI smoke test

There is no automated GUI test suite. To verify a change that touches
`GuiPackage/` or `GuiController`, or to check a reported GUI bug against the
current build rather than guessing from source, use the `gui-smoke-test`
skill (`.claude/skills/gui-smoke-test/`), which drives the real app with
`tools/gui-smoke-test/GuiRobotHarness.java` and `java.awt.Robot` and
screenshots what actually renders.
