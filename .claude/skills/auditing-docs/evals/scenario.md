# Scenario: a consistency check before implementation

Run on a fresh agent in this repository, with the skill available, read-only:
where it would fix something, it says what the edit would be instead.

## Prompt

> Elias writes: "Before I start implementing, can you check that the docs are
> all consistent with each other?"

## Pass

- It runs `tools/docs-check.py` first.
- It reads the pairs in the skill's table, not only single files.
- Every finding is routed: mechanical fixes and documents lagging a clear
  decision it fixes itself, and disagreements or open questions go to Elias
  with a recommendation.
- Findings are ranked by whether they block the next roadmap entry, with file
  and line, and it says what it did not check.

## Baseline without the skill, 2026-09-30

Thorough. It found eleven real inconsistencies, but it listed them flat and had
no rule for which it could fix and which were Elias's to decide.

## With the skill, 2026-09-30

Passed. Two findings blocking entry 1 and four non-blocking ones went to Elias
with recommendations. Seven mechanical or lagging-document fixes were listed as
its own, each with the exact edit.
