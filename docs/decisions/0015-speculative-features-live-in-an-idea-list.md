# 0015. Speculative features live in a committed idea list

## Status

Accepted, 2026-09-27. Extends
[0001](0001-documentation-splits-into-three-artifacts.md).

## Context

[0001](0001-documentation-splits-into-three-artifacts.md) split the
documentation three ways: the design as it is meant to be, the log of settled
choices, and a disposable plan. Nothing holds a feature that is wanted and not
decided. Those have been landing in conversation and in the plan, which is
gitignored, so they do not travel between machines and they go when the plan is
rewritten.

A section at the end of `DESIGN.md` was the cheaper alternative, needing no new
file. `DESIGN.md` is present tense about a system that is meant to exist, so a
speculative section blurs the one thing that document promises.

The Nygard format the log uses does not fit an idea, which has no decision and no
consequences yet.

## Decision

`docs/ideas/`, committed, one file per idea, named in kebab-case rather than
numbered, because ideas get promoted and dropped and their order means nothing.

Each file carries a status line, either `idea`, `planned`, or `built` with a link
to the entry that settled it, then four sections. What it does. Why it is
interesting, meaning which emergent effect it buys. What it would touch. Open
questions.

A README indexes them, as in the decision log. An adopted idea becomes a decision
entry and keeps its file, with the status line pointing at the entry.

## Consequences

An idea has somewhere to go the moment it appears, so a conversation can end in
neither a decision nor a loss.

Four artifacts to keep consistent instead of three, and the failure mode is an
idea that was built and still says `idea`.

The list is committed, so a visitor to the repository reads it as a roadmap. It
is not one, which the README says.

An idea file is not a design. It records enough to restart the thinking, and
stops there.
