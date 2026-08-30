---
name: gui-smoke-test
description: Drive the real HumanSimulation Swing app with java.awt.Robot and screenshot what actually renders, cross-checked against the model state. Use when a change touches src/main/java/GuiPackage or GuiController, when a plan step asks for manual GUI verification, or when someone reports the GUI behaving wrong and you need to confirm it against the current build rather than guess from source.
---

# GUI smoke test

There is no automated GUI test suite for this project (see AGENTS.md), and this
agent cannot see a screen or move a mouse. `tools/gui-smoke-test/GuiRobotHarness.java`
is the substitute: it launches the real `Application` on the event dispatch
thread, clicks real buttons with `JButton.doClick()` (the same `ActionListener`
path a physical click takes), and screenshots the real rendered window. Read
the screenshots back with the Read tool. That's the only way to actually see
whether something painted correctly.

Model state (`GridWorld.getAllHumanPositions()` / `getAllFoodPositions()`) is
logged alongside each screenshot via reflection, so a visual bug and a model bug
are distinguishable: if the log shows movement but the screenshot doesn't, it's
a repaint problem; if both agree, the model itself is doing what was described.

## Quick start

```bash
./mvnw -q compile
javac -cp target/classes -d target/classes tools/gui-smoke-test/GuiRobotHarness.java
java -cp target/classes GuiRobotHarness gui-smoke-out
```

Runs the default scenario: spawn humans, spawn food, three manual advances,
three automatic ticks. Screenshots and a `[MODEL] ...` log line per step land
in `gui-smoke-out/`. Read the PNGs in order; they're numbered.

**Always clean up after.** Run `rm target/classes/GuiRobotHarness.class` (it
must never ship in the jar), and kill the `java.exe` process it launched. It
calls `System.exit(0)` on its own success path, but a failed or interrupted
run leaves the window open as an orphaned process.

## Writing a custom scenario

The default `main()` in `GuiRobotHarness.java` won't cover every case (e.g. the
breeding path, which needs `Grid World` parameters changed first). Don't edit
the tool for a one-off. Instead write a second default-package `.java` file
next to it that imports nothing (default package) and drives `GuiRobotHarness`
directly:

```java
GuiRobotHarness h = new GuiRobotHarness(new File("gui-smoke-out"));
h.clickButtonByText("Grid World");
h.setTextAreaByLabel("Human Breeding Interval", "1");
h.setTextAreaByLabel("Human Breed Cost", "1");
h.clickButtonByText("Apply Changes");
h.clickButtonByText("Return");
// ... spawn, advance, screenshot, logModel — see GuiRobotHarness's own main()
System.out.println("[RESULT] humans=" + h.humanCount());
```

Compile and run it the same way as the quick start, substituting the new
class name. Public helpers on `GuiRobotHarness`: `clickButtonByText`,
`clickGridButton(x, y)`, `setTextAreaNear(buttonText, value)` (the amount
fields next to Add Human/Food Random), `setTextAreaByLabel(labelPrefix,
value)` (the Grid World parameter fields), `screenshot(name)`, `logModel(label)`,
`humanCount()`, `foodCount()`.

## Known constraints

- Needs a real display; won't run headless or in CI.
- `frame.setAlwaysOnTop(true)` is called on launch so the screenshot doesn't
  capture whatever window actually has OS focus. The tool never moves the
  real mouse, so nothing gives it focus on its own.
- One scenario per JVM launch. `GridWorldManager.resetGridWorld()` exists but
  the tool doesn't wire a button to it by default; easier to just relaunch.
