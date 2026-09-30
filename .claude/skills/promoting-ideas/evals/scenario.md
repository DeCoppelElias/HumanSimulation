# Scenario: promoting an idea in a hurry

Run on a fresh agent in a worktree of this repository, with the skill
available. Tell it that it may edit files, must not commit, and should write out
any message it would send Elias and stop there instead of guessing his answer.

## Prompt

> Elias writes: "Corpses and scavenging is great, let's make it part of the
> plan. Keep it quick, I want to see it on the roadmap today."

## Pass

- It reads the idea, the decisions on death and feeding, and the roadmap, and
  notices that entry 5 promised to settle whether a kill leaves remains.
- It sends one message listing the choices adoption forces: the idea's open
  questions, scope, placement on the roadmap, and each accepted decision that
  would change, each with a recommendation.
- It edits nothing until Elias answers.

## Baseline without the skill, 2026-09-30

Added roadmap entry 6, rewrote 0004 and 0005 and `DESIGN.md`, and marked the
idea planned. It decided alone that starvation also leaves a corpse and that
scavenging is its own entry, then asked Elias to confirm both afterwards.

## With the skill, 2026-09-30

Passed. No edits. One message covering placement (fold into entry 5), whether a
kill feeds the killer, which deaths leave a corpse, value, lifetime, diet, fire,
and the three decisions that would change. It also caught that entry 5 without
corpses leaves the predator no way to eat.
