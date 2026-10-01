---
name: promoting-ideas
description: Moves a HumanSimulation idea from docs/ideas/ onto the roadmap and into the decision log. Use when Elias says to adopt, plan or schedule an idea, to "add it to the roadmap" or "make it part of the plan", or when a roadmap entry is about to build something that only exists as an idea.
---

# Promoting ideas

## Overview

Promotion commits the project to something. When Elias says "add it", he has
approved adopting the idea. He has not approved the choices that adopting it
forces, and those are his to make.

Recording a new idea needs no skill and no permission: write the file in
`docs/ideas/` in the format its README gives, or extend an existing idea that
covers it.

## Steps

1. Read the idea file, every decision it links, and `docs/roadmap.md`. Note any
   roadmap entry that says it will settle this idea's question.
2. List the choices adoption forces:
   - each open question in the idea file: answer now, or defer with the roadmap
     entry recording that it gets a log entry before it is built;
   - scope, meaning what of the idea is in and what waits;
   - placement: a new roadmap entry or folded into an existing one, and what it
     depends on;
   - every accepted decision whose text would change.
3. Grill Elias on that list with the grilling skill (REQUIRED SUB-SKILL), one
   choice per question with your recommendation, and wait for every answer.
4. Apply them:
   - decision changes through the changing-decisions skill (REQUIRED
     SUB-SKILL), which updates `DESIGN.md` too;
   - the roadmap entry: title, `Status: not started.`, what it builds, what it
     depends on, and a See list of the decisions and the idea file;
   - any roadmap entry that promised to settle the question, rewritten to say
     where it is settled now;
   - the idea's status line: `Status: planned, roadmap entry N.` The file
     stays.
5. Run `python tools/docs-check.py` until it exits 0.

## Red flags

| Thought | Reality |
|---|---|
| "He wants it today, I'll decide and he can override" | An override after the fact means he reviews ten files instead of answering four questions. Ask. |
| "The idea's wording implies the answer" | Wording in an idea is not a decision. List it. |
| "It only extends an accepted decision" | Extending one changes it. It goes in the list. |
| "The open questions can wait" | They can. Say so in the roadmap entry, and let him say which ones. |
