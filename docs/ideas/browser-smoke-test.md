# Browser smoke test

Status: idea.

## What it does

An agent opens the real page in a browser, clicks through the controls, takes
screenshots and checks what it sees against the world's snapshot from the API.
It replaces the Swing smoke test, which drove the 2022 window with
`java.awt.Robot` and goes with the 2022 code.

## Why it is interesting

Being pretty to watch is a goal in `DESIGN.md`, and nothing automated looks at
the page. Vitest checks the frontend's logic and JUnit checks the API, so a
sprite drawn on the wrong tile or a glide that never ends passes both. Today a
person watching the page is the only check.

## What it would touch

A project skill in `.claude/skills/`, driving either a browser tool the agent
already has or Playwright. If it runs in CI, the build downloads browsers on the
Linux and Windows runners, which slows every build.

## Open questions

Whether it stays a skill an agent runs on demand or becomes a test in CI.

How a screenshot is judged: by an agent looking at it, or by comparing against
stored images, which breaks on every intended visual change.
