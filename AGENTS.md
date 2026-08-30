# AGENTS.md

Java Swing application that simulates humans searching for food on a 2D grid.
The simulation was written in September 2022 and its behaviour is kept as it
was. README.md describes what the application does and how to use it.

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

CI runs `./mvnw -B verify` on pushes to `master` and on pull requests. A `v*`
tag builds the jar and attaches it to a GitHub Release.

## Formatting

Spotless with palantir-java-format, ratcheted against `origin/master`, so only
files you actually change get formatted. Run `./mvnw spotless:apply` on your own
changes.

Never format the whole tree. That would rewrite all 29 files and move
`git blame` off the 2022 commits, which is what the ratchet exists to prevent.

## Two things to know before writing tests

The simulation seeds no RNG. `FindFoodBehaviour`, `MovementBehaviour`,
`FoodBehaviour` and `Human.createChildSpecific` each construct `new Random()`
inline, so runs cannot be reproduced. That is why `fightingForFoodTest` asserts
the grid is either of two states. Any test of behaviour or selection needs a
seed injected first.

`v1.0-original-2022` and the `original-2022` branch hold the 2022 version, with
the old IntelliJ files, the flat `src/` layout and a committed jar. Both are
immutable. Land changes on top of `master`.
