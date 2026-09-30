# 0019. Workflows are skills, and invariants are scripts

## Status

Accepted, 2026-09-30.

## Context

[0001](0001-documentation-splits-into-five-artifacts.md) splits the docs into
five artifacts and names the cost: moving content between them is where they
drift, and missing `DESIGN.md` when a decision changes is the common failure.
Nothing said how the moves are made, or which of them an agent may make without
asking.

A single process document was considered. It is the cheapest to write, and it
is a request an agent reads once and drifts from, the way 0001 predicts for any
document. Hooks for every rule were considered too. They guarantee what they
check, and most of these rules are judgement, such as whether a question is the
same topic as an existing entry, which a hook cannot decide.

Six flows were tried on a fresh agent that had `AGENTS.md` and no workflow
skill. Recording an idea and handling a bug report came out right: the agent
extended the idea that already covered the thought, and it found that the
reported bug was designed behaviour before touching code. Promoting an idea and
changing a decision came out wrong the same way. Under time pressure the agent
made Elias's choices for him, rewrote accepted decisions, and listed what he
should confirm afterwards. Implementing a roadmap entry skipped review, the doc
audit and any retrospective at the finish. A consistency audit found real drift
and had no rule for which findings it could fix itself.

## Decision

A flow a fresh agent gets wrong is a project skill in `.claude/skills/`, written
to the Agent Skills format and tested the way superpowers:writing-skills
describes: a scenario run without the skill, the skill written against what
went wrong, the scenario run again with it. Each scenario is committed in the
skill's `evals/` folder, and an edit to a skill reruns it.

There are four: `changing-decisions`, `promoting-ideas`,
`implementing-roadmap-entries` and `auditing-docs`. Recording an idea and fixing
a bug have none, because `AGENTS.md` and the ideas README already carry them.

A rule a regex can check is a script. `tools/docs-check.py` checks links,
indexes, numbering and status lines, and runs in the pre-commit hook and in CI.

An agent may record an idea, draft any change on a branch, run the audit and fix
what it finds mechanical, all without asking. It asks Elias before promoting an
idea, changing the text of an accepted decision, adding or reordering roadmap
entries, and before any push, merge, pull request or tag. Asking means stating
the choices with a recommendation and waiting, not deciding and asking for
confirmation afterwards.

## Consequences

`AGENTS.md` names the skills, so an agent that does not load Claude Code skills
can still find and read them.

A skill describes how the documents relate, so a change to that relationship in
0001 means editing the skills too. The audit reads skills, and `docs-check.py`
checks their links.

The approval line puts Elias in the loop for every change to what the project is
committed to, and nowhere else. Decisions move no faster than he answers, which
is the point.

A flow that starts going wrong earns a skill then, through the retrospective at
the end of each roadmap entry, rather than in advance.
