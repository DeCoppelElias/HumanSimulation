# AGENTS.md

Java Swing application that simulates humans searching for food on a 2D grid.
The simulation was written in September 2022 and its behaviour is kept as it
was. README.md describes what the application does and how to use it.

## Layout

- `src/main/java/Main.java` entry point. `src/main/java/Application.java` builds
  the 20x20 `GridWorld`, its `GridWorldManager` and the `GuiController`.
- `src/main/java/SimulationApplication/` the model: grid world, grid tiles,
  entities, human behaviour and mutation.
- `src/main/java/GuiPackage/` the Swing UI. `GuiController` sits between the
  panels and the model and owns the automatic-advance scheduler.
- `src/main/java/DataAnalytics/` population counts over time and the JFreeChart
  line chart.
- `src/test/java/Test/` JUnit tests.
- `src/main/resources/` `Human.png` and `Food.jpg`. Loaded with
  `getResource("/Human.png")`, so they have to stay at the classpath root.

## Build and run

Maven, through the wrapper. No local Maven install is needed, and there is no
`mvn` on PATH on the development machine.

    ./mvnw verify     compile, check formatting, run tests
    ./mvnw package    also build target/HumanSimulation.jar
    java -jar target/HumanSimulation.jar

Java 21. Both dependencies are pinned in `pom.xml`:

- `org.jfree:jfreechart:1.5.3`
- `org.junit.jupiter:junit-jupiter:6.1.3` (test)

The enforcer plugin fails the build on a version range or a snapshot, so
dependency resolution stays reproducible. Let Dependabot propose upgrades rather
than editing versions by hand.

## Tests

`src/test/java/Test/GridWorldTest.java`, run by Surefire under `./mvnw test`.

The simulation seeds no RNG. `FindFoodBehaviour`, `MovementBehaviour`,
`FoodBehaviour` and `Human.createChildSpecific` each construct `new Random()`
inline, so no injection point exists and a run cannot be reproduced. That is why
`fightingForFoodTest` has to assert the grid is either of two states. Any new
test of behaviour, mutation or selection needs a seed injected first.

## Formatting

Spotless with palantir-java-format, in ratchet mode against `origin/master`.
Only files that actually change get formatted, so the untouched 2022 sources
keep their original bytes and `git blame` still reaches the commits that wrote
them.

    ./mvnw spotless:apply    format the files you changed

`./mvnw verify` runs `spotless:check` and fails on a badly formatted file. The
ratchet resolves a git ref, so CI checks out with `fetch-depth: 0`.

Do not run `spotless:apply` across the whole tree. A full format would rewrite
all 29 files and move `git blame` off the 2022 commits, which is exactly what
the ratchet exists to prevent.

## CI and releases

`.github/workflows/ci.yml` runs `./mvnw -B verify` on pushes to `master` and on
pull requests. `.github/workflows/release.yml` builds the jar on a `v*` tag and
attaches it to a GitHub Release. The jar is no longer committed to the tree.

## Preserved original

`v1.0-original-2022` and the `original-2022` branch hold the September 2022
version, including the old IntelliJ module files, the flat `src/` layout and the
committed jar. Treat both as immutable and land changes on top of `master`.
