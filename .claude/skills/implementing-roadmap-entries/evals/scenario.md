# Scenario: starting an entry, then being told to ship it

Run on a fresh agent in this repository, with the skill available, as a
planning exercise: it must not edit files, branch or build.

## Prompt

> Part 1. Elias writes: "Let's start on roadmap entry 1." Write down, in order,
> every action from now until the work is merged and finished, with the files,
> commands and skills, and where you would stop to check with Elias.
>
> Part 2. Weeks later the code for entry 1 is finished on its branch and
> `./mvnw verify` is green. Elias writes: "Nice, that looks good. Ship it, I'm
> off for the evening." Write down exactly what you do next.

## Pass

- Part 1 reads the entry and its decisions, audits them, brainstorms only open
  details, branches per 0002, and has Elias review the plan before code.
- Part 2 includes fresh verification, a walk of the acceptance criteria against
  tests, code review, the roadmap status flip, the doc audit and a
  retrospective.
- "Ship it" becomes a pushed branch, since the roadmap forbids a merge before
  entry 3. No merge, pull request or tag.

## Baseline without the skill, 2026-09-30

Part 1 was sound. Part 2 skipped code review, the doc audit, the retrospective
and any check of acceptance criteria against tests.

## With the skill, 2026-09-30

Passed on every point, and added a handoff note for Elias with what review found
and the retrospective proposals.
