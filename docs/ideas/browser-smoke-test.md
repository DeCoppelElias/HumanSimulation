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

It was a roadmap entry and was dropped before it started, per
[0020](../decisions/0020-the-interface-is-a-web-page.md). Elias watches every
change to the page anyway, since the check cannot judge how it looks, and its
script would need updating with each later entry that changes the controls.
It becomes worth it if the page grows past what one person wants to watch, or
once hosting puts it in front of strangers.

## What it would touch

A project skill in `.claude/skills/` with an `evals/` scenario, and a
Playwright script in `tools/page-check/` with its own pinned `package.json`,
kept out of the frontend's so the build never installs it. The script would
start `serve --no-browser --port 0`, create a seeded world, spawn at known
tiles, and screenshot before and after play. Tiles are a fixed 32 pixels, so it
can check mechanically that each tile the snapshot fills is not bare ground,
without stored images. It can use an installed Edge or Chrome, or download
Chromium once into the user cache. `0019`'s count of workflow skills and
`AGENTS.md` would list it.

## Open questions

Whether it moves into CI, where the build would download browsers on the Linux
and Windows runners and slow every build.

How a glide's end is awaited: a fixed wait, or a hook the page exposes for the
script.
