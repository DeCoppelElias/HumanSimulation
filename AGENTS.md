# AGENTS.md

Java Swing application that simulates humans searching for food on a 2D grid.
It was written in September 2022. README.md describes what the application does
and how to use it.

## Where to find things

`DESIGN.md` describes the system as it is meant to be: what it is for, what it is
not, and how the model fits together. Read it before changing anything in the
model layer, and before proposing a feature.

`docs/decisions/` is the decision log, one entry per settled choice with what was
rejected and why. Read the entry before reopening a design question.
`docs/decisions/README.md` indexes them.

The model layer is being rebuilt, so the tree does not match `DESIGN.md` yet. The
work plan under `docs/superpowers/plans/` tracks where that stands. It is
gitignored and may be missing from a fresh clone.

Keep this file mechanical. Anything about why the system is shaped the way it is
belongs in `DESIGN.md` or the decision log.

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

Never format the whole tree. That would rewrite all 29 files and move
`git blame` off the 2022 commits, which is what the ratchet exists to prevent.

`ResourceLoadingTest` guards the packaging contract, not the simulation: the
icons have to stay directly under `src/main/resources` or `GridPanel` fails at
runtime while everything still compiles.

## Two things to know before writing tests

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
