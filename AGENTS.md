# AGENTS.md

Java Swing application that simulates humans searching for food on a 2D grid.
Written in September 2022 and kept as it was. README.md describes what the
application does and how to use it.

## Layout

- `src/Main.java` entry point. `src/Application.java` builds the 20x20
  `GridWorld`, its `GridWorldManager` and the `GuiController`.
- `src/SimulationApplication/` the model: grid world, grid tiles, entities,
  human behaviour and mutation.
- `src/GuiPackage/` the Swing UI. `GuiController` sits between the panels and
  the model and owns the automatic-advance scheduler.
- `src/DataAnalytics/` population counts over time and the JFreeChart line chart.
- `src/Test/` JUnit 5 tests.
- `resources/` `Human.png` and `Food.jpg`. Loaded with `getResource("/Human.png")`,
  so `resources` has to sit at the root of the classpath.
- `out/artifacts/MyFirstGui_jar/MyFirstGui.jar` a committed runnable fat jar.

## Build and run

There is no Maven or Gradle build. The project is an IntelliJ module
(`MyFirstGui.iml`) at language level 17, and its two dependencies are declared
as paths into the local Maven cache:

- `org.jfree:jfreechart:1.5.3`
- `org.junit.jupiter:junit-jupiter:5.8.1`

To compile outside IntelliJ, put those jars from `~/.m2/repository` on the
classpath and hand javac every file under `src/`:

    javac -encoding UTF-8 -cp "<jars>" -d <outdir> $(find src -name '*.java')

Then run it, with `resources` on the classpath and `;` as the separator on
Windows:

    java -cp "<outdir>;resources;<jars>" Main

Or run the committed jar: `java -jar out/artifacts/MyFirstGui_jar/MyFirstGui.jar`.

## Tests

`src/Test/GridWorldTest.java` uses JUnit 5. It runs through IntelliJ. No
command-line runner is configured, so running the tests from a shell means
fetching `junit-platform-console-standalone` first.
