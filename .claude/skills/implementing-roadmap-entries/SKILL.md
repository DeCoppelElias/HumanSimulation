---
name: implementing-roadmap-entries
description: Takes a HumanSimulation roadmap entry from first read to finished and merged, wiring the superpowers planning, TDD, review and branch skills to this project's docs. Use when starting, resuming or finishing an entry in docs/roadmap.md, or when told to "ship", "wrap up" or "merge" roadmap work.
---

# Implementing roadmap entries

## Overview

The superpowers skills carry the engineering. This skill adds what they cannot
know: which documents an entry is bound by, and which of them change when the
entry lands. Agents do the start well and skip the finish, so the finish is a
checklist.

## Starting

1. Read the entry, every decision in its See list, and the matching sections of
   `DESIGN.md`. Look in `docs/superpowers/plans/` for a plan already in
   progress.
2. Run the auditing-docs skill over those documents. A contradiction found now
   costs a message. Found halfway through, it costs a rewrite.
3. Grill Elias on the details the decisions leave open with the grilling skill
   (REQUIRED SUB-SKILL), and apply its result before planning. Settled choices
   are not reopened. A gap that turns out to be a design choice goes through
   the changing-decisions skill.
4. Branch as the roadmap says. Per 0002, the rebuild lives on one branch until
   entry 3 merges it to `master`.
5. Write the plan with superpowers:writing-plans. It lists every acceptance
   criterion in the entry, regression rules included, next to the task that
   meets it. Elias reviews the plan before any code.

## Building

superpowers:test-driven-development for every task, as 0012 requires. Verify
with `./mvnw verify` and the `run` command once it exists. Until the browser
smoke test in `docs/ideas/` is built, a change to what the page shows needs
Elias to watch it, so say what to look at.

## Finishing

Do every step, in order, before calling the entry done. "Ship it" does not
shorten the list.

1. superpowers:verification-before-completion, with fresh command output.
2. Walk the plan's acceptance criteria. Each one names a test that failed
   before its code existed. A criterion with no test is not met.
3. superpowers:requesting-code-review on the branch, then
   superpowers:receiving-code-review on what comes back.
4. Documents:
   - the entry's status in `docs/roadmap.md` becomes `done`;
   - an idea this entry built becomes `Status: built, [NNNN](../decisions/NNNN-slug.md).`,
     linking the entry that settled it;
   - a choice made during the work goes through changing-decisions;
   - run the auditing-docs skill.
5. Retrospective. Anything an agent got wrong twice during the entry becomes an
   `AGENTS.md` line, a skill edit (rerun that skill's `evals/` scenario), or a
   check in `tools/`. Tell Elias which, and why.
6. superpowers:finishing-a-development-branch. Push, merge, open a PR or tag
   only when Elias says so. If the roadmap forbids a merge yet, "ship it" means
   push the branch.

If the session ends before the entry does, use the handoff skill. The plan is
gitignored and does not travel between machines.
