# Skill review

Status: idea.

## What it does

Checks every project skill in `.claude/skills/` against five questions:

- Does a fresh agent still get the flow wrong without it, with the upstream
  plugins installed? A skill whose scenario now passes without it is deleted.
- Does an upstream skill already do it? Then the project skill keeps only what
  upstream cannot know and names the upstream skill for the rest.
- Is each step about this project? A preference of Elias's that holds
  everywhere goes in his global `CLAUDE.md`, navigation goes in `AGENTS.md`,
  and a step a calling skill already takes is dropped.
- Does it still describe how the documents relate, after any change to them?
- Is every skill it calls declared, in `.claude/settings.json` and `AGENTS.md`,
  so a fresh clone has it?

What the review already expects to find:

- `grilling` goes once its scenario passes without it. The command for reading
  deleted 2022 code moves to `AGENTS.md`, the implementing and promoting skills
  name the upstream `grilling` from mattpocock/skills, and the interview style,
  one question at a time with its context inside the question, moves to the
  global `CLAUDE.md` if it is still wanted.
- The retrospective step in implementing-roadmap-entries points at the upstream
  `retro`.
- promoting-ideas, changing-decisions and auditing-docs follow
  [decision topics](decision-topics.md) and
  [stable references](stable-references.md) once those land.
- superpowers and mattpocock/skills are declared as dependencies.

## Why it is interesting

A skill is written against what a fresh agent got wrong at the time. Upstream
skills improve, the documents change, and preferences end up in the project
that belong to the person, so a skill can keep loading after its reason is
gone. Two skills with one job compete to trigger and drift apart, and the
project `grilling` already shares its name with an upstream one.

To keep it that way, each `SKILL.md` opens its overview with a `Builds on:` line
naming the upstream skill it wraps, or `none`, and `tools/docs-check.py` fails
a skill without one. The retrospective in [process budget](process-budget.md)
covers skills too, and a skill that comes up there has its scenario rerun.
There is no review schedule beyond that.

## What it would touch

Every skill in `.claude/skills/` and its `evals/` scenario, `AGENTS.md`,
`.claude/settings.json`, `tools/docs-check.py` and its tests, and a line in
[0019](../decisions/0019-workflows-are-skills-and-invariants-are-scripts.md)
on what a project skill may hold.

## Open questions

Whether one question at a time is still preferred over the upstream rounds,
which ask every question that can be asked now in one numbered list.

Whether plugin versions are pinned, so an upstream change cannot alter a
workflow silently.

Whether the duplicate skills in Elias's user setup are cleaned up in the same
pass. They live outside this repository but trigger inside it.
