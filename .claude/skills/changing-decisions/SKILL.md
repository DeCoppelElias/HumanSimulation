---
name: changing-decisions
description: Rewrites a HumanSimulation design decision in docs/decisions/ in place and carries the change through DESIGN.md, the roadmap and the ideas. Use when Elias changes a settled choice, when a new design question comes up, when implementation forces a choice the log did not make, or when asked to "update the docs" for a design change.
---

# Changing decisions

## Overview

Each topic has exactly one entry. Changing a choice means rewriting that entry
so it states the current decision and why, then making every other document
agree. Git keeps what it said before.

An accepted decision changes only when Elias has agreed to the change, knowing
what it costs. Drafting the change on a branch for him to read is fine.

## Before editing anything

1. Find the topic. Search entry bodies as well as the index, since titles name
   the decision rather than the topic: `grep -ril "<term>" docs/decisions`.
   Write a new entry only when no existing topic covers the question. If two
   entries could hold it, ask Elias which one.
2. Find what depends on it: every file that cites the entry number or uses the
   terms it defines, across `DESIGN.md`, `AGENTS.md`, `docs/roadmap.md`,
   `docs/ideas/` and the other entries.
3. Send Elias one message before writing:
   - what the change touches, from step 2;
   - any consequence that works against the vision in `DESIGN.md` or a gate on
     the roadmap;
   - his reason, if he has not given it. An entry is useless without its
     reason, and inventing one is worse than asking.

   Then wait for his go-ahead.

"Quick change" and "should only take a minute" do not skip step 3. The question
is one message, and a wrong rewrite across ten files costs more than that.

## Writing the change

- Keep the number. Keep the accepted date on the status line and set
  `Revised <today>`.
- The old choice becomes a rejected alternative in Context, with the reason it
  lost. Keep every reason that still holds.
- If the title no longer states the decision, retitle it, `git mv` the file to
  the new slug and update the index line.
- Something the old decision carried that is still wanted but has lost its home
  goes to `docs/ideas/`, not into the bin.
- Update `DESIGN.md` in the same change, in present tense with no history.
- Update every dependent from step 2: roadmap entries naming the old choice,
  ideas that assume it, entries that cite it.
- A choice Elias has not made stays open. Write it as an open question, not a
  decision.

## Check

    python tools/docs-check.py

Exit 0 means links, indexes and status lines agree. Then reread each dependent
from step 2 for sentences that still describe the old choice, which the script
cannot see.

## Red flags

| Thought | Reality |
|---|---|
| "He said it's quick, just do it" | Speed of writing is not the cost. Ask first. |
| "I'll note the risk in Consequences" | A risk he reads after the rewrite is a risk he did not weigh. Raise it before. |
| "I don't know why, I'll describe it neutrally" | Ask for the reason. |
| "This needs its own entry" | Only if no topic covers it. Search bodies, not titles. |
| "That paragraph no longer applies, delete it" | If what it wanted is still wanted, move it to an idea. |
