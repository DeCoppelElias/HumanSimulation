---
name: grilling
description: Interviews Elias one question at a time, each with a recommendation, until every choice a HumanSimulation roadmap entry or idea leaves open is settled, and flags gaps in the docs and workflows on the way. Use before planning a roadmap entry, before promoting an idea, or when Elias asks to be grilled or to "work out the details".
---

# Grilling

## Overview

The decision log settles the architecture and leaves details open. A plan
written over open details hides choices that belong to Elias inside tasks. A
grill finds each one and has him make it, in the order the choices depend on
each other, before anything is planned or written.

## Before the first question

1. Read the roadmap entry or idea, every decision it links, the matching
   sections of `DESIGN.md`, and the code it will replace or touch. Once the
   rebuild has deleted the 2022 code it is still on `master` and the
   `v1.0-original-2022` tag, so read it with `git show master:<path>`.
2. List the choices left open. Answer every question the code or the docs can
   answer by reading them, and never ask it.
3. Note gaps: rules with no entry to land in, docs that disagree, a workflow or
   tool that the work will break. Open the session with them, in short, then
   come back to each where it bears on a question.

## Asking

- One topic per question, ordered so nothing asked depends on something not
  yet settled.
- Each question gives the context needed to answer it and a recommendation with
  its reason. Put the recommended option first.
- Use the question tool when the session has one. Text written before the
  question box is hidden behind it, so the context goes inside the question
  itself. Say what the previous answer settled at its start.
- When Elias asks for an explanation, or answers with a question, reply in a
  plain message with no question tool, and ask again in the next turn.
- An answer that changes an earlier premise reopens the questions built on it.
  Say which, and ask again.
- When he asks for options with good and bad points, research them first and
  give each its pros and cons with a recommendation.

## While grilling

Record an idea that comes up and is deferred in `docs/ideas/` straight away,
since that needs no approval. Write nothing else, even though 0019 would allow
drafting on a branch: a half-finished grill leaves half-applied answers.
Accepted decisions, the roadmap and `DESIGN.md` change only after the grill
ends.

## Ending

When nothing is left open, send a summary of every choice, grouped, and the
document changes they create. Then apply them only after Elias says go, on a
branch:

- decisions through the changing-decisions skill (REQUIRED SUB-SKILL);
- an idea being promoted through the promoting-ideas skill from its step 4,
  since the grill already asked its questions;
- roadmap text, such as acceptance lines, directly;
- then the auditing-docs skill.

## Red flags

| Thought | Reality |
|---|---|
| "I'll ask the three related questions together" | One topic per question. A bundled answer hides which part he disagreed with. |
| "The code would tell me, but asking is quicker" | Read the code. His time is the scarce part. |
| "He picked an option, so the earlier answers stand" | Check whether it changed a premise of an earlier answer. |
| "I'll put the background above the question" | He cannot see it. Put it in the question. |
| "This choice is small, I'll decide it" | Decide only the cosmetic, and say so in the summary. |
