---
name: auditing-docs
description: Checks HumanSimulation's DESIGN.md, decision log, roadmap, ideas and AGENTS.md for contradictions and drift, and routes each finding to whoever may fix it. Use before starting a roadmap entry, after changing a decision or promoting an idea, or when asked whether the docs are consistent.
---

# Auditing docs

## Overview

The five documents from 0001 each hold one kind of truth, and the split drifts
where two of them describe the same thing. A script catches the mechanical
drift. This skill covers the rest and decides who fixes what.

## Steps

1. Run `python tools/docs-check.py` and fix everything it prints. Its findings
   are mechanical by construction.
2. Read the pairs where drift happens:

   | Pair | Question |
   |---|---|
   | each decision and `DESIGN.md` | Does the design state what the entry decided? |
   | decision and decision | Do shared types, names and numbers agree? |
   | roadmap entry and its See list | Does it build what the decisions say, and do its inputs exist by then? |
   | roadmap and ideas | Is an idea partly planned or built while still marked `idea`? |
   | any doc and `DESIGN.md` vocabulary | Is a domain word used that `DESIGN.md` never defines, per 0003? |
   | `AGENTS.md` and the tree | Do its paths, counts and commands still hold? |

   When scoped to one entry or change, read only the pairs it touches.
3. Route every finding:
   - Mechanical, such as a broken link, bad formatting, a stale count or an
     index line: fix it.
   - A document lagging a clear, accepted decision: bring the lagging document
     in line, and say which document you changed.
   - Two documents disagreeing on a choice, or a question nothing answers: do
     not pick. Report it with a recommendation. Once Elias chooses, the fix goes
     through the changing-decisions skill.
4. Report findings ranked by whether they block the next roadmap entry, each
   with file and line, then what was fixed, then what was not checked.
