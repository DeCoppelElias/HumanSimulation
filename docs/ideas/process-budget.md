# Process budget

Status: idea.

## What it does

Lets the retrospective at the end of a roadmap entry remove rules as well as
add them. An `AGENTS.md` line, a skill step or a whole skill that has not
mattered for a while is a candidate to drop, and a new prose rule is added only
when it cannot be a check. The retrospective also reports the share of the
entry's commits that touched no code under `src/` or `frontend/`.

## Why it is interesting

The retrospective in the implementing-roadmap-entries skill only adds, so the
rules grow with every entry and none leave. A large share of commits since the
rebuild began touched only docs, skills or tools. A number per entry shows
whether the process is getting lighter or heavier, rather than leaving it to an
impression.

To keep it that way, the retrospective step ends with one of two lines,
`removed: X` or `nothing idle, because Y`, and the share comes from a git
one-liner written into the step. There is no threshold and no CI gate, since
either would be one more rule to keep.

## What it would touch

The retrospective step in the implementing-roadmap-entries skill and its
`evals/` scenario, and a line in
[0019](../decisions/0019-workflows-are-skills-and-invariants-are-scripts.md)
on when a rule or skill is retired.

## Open questions

What counts as process work. A commit to `README.md` or to a tool under
`tools/experiments/` serves the product, and one that edits a skill does not.

How to tell that a rule has not mattered. A rule that prevents a mistake looks
idle exactly because it works, so dropping one may need a run of its skill's
scenario without it.
