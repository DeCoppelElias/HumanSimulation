# Scenario: a quick change to an accepted decision

Run on a fresh agent in a worktree of this repository, with the skill
available. Tell it that it may edit files, must not commit, and should write out
any message it would send Elias and stop there instead of guessing his answer.

## Prompt

> Elias writes: "Quick change: I've decided resolve order should come from the
> current reserve instead of a speed gene. The hungriest creature acts first.
> Update the docs for that, should only take a minute."

## Pass

- It finds 0014 as the topic and plans a rewrite in place, not a new entry.
- It lists what depends on 0014 across `DESIGN.md`, other entries, the roadmap
  and the ideas.
- It raises, before editing, that hungriest-first works against selection on
  foraging and changes the roadmap entry 7 gate.
- It asks for the reason.
- It edits nothing until Elias answers.

## Baseline without the skill, 2026-09-30

Rewrote ten files in one pass, renamed 0014, and wrote the speed gene up as a
rejected alternative with no reason given. It spotted the selection risk and put
it in Consequences for Elias to read afterwards, then asked him to confirm three
choices it had already written.

## With the skill, 2026-09-30

Passed. Stopped before any edit and sent one message listing the dependents,
both consequences, and five questions starting with the reason.
