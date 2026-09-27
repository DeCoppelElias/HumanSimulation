# 0001. Documentation splits into four artifacts

## Status

Accepted, 2026-09-03. Revised 2026-09-27.

## Context

Design notes for this project have so far been single-phase work products kept
outside git, with the durable record living in agent memory. Memory is a fine
backstop for six decisions and a bad one for a document, and a gitignored file
means every clone and every CI run has a different idea of what the project is.

Four kinds of content were getting mixed. Why the project exists changes rarely
and wholesale. A decision changes rarely and is useless without its reasoning. A
feature that is wanted and not decided has no decision and no consequences yet.
A description of the current tree is stale on the next commit. A single document
holding all four churns constantly and stops being trusted.

An append-only log was considered, where a changed choice gets a new entry and
the old one is marked as superseded. It keeps the trail visible in the log
itself. It also makes the current answer to a question the sum of a chain of
entries, and before anything is built, choices change often enough that the
chains outnumber the topics. Git already keeps the trail.

A section at the end of `DESIGN.md` was considered for ideas. `DESIGN.md` is
present tense about a system that is meant to exist, so a speculative section
blurs the one thing that document promises.

## Decision

Four artifacts with four jobs.

`DESIGN.md`, at the repository root and committed, describes the system as it is
meant to be. Present tense, no history, no progress, no alternatives. Its
readers are Elias and Claude. It holds vision, non-goals and architecture, and
no code.

`docs/decisions/`, committed, holds the log, one entry per topic in Nygard
format. When the choice on an existing topic changes, its entry is rewritten to
state the current decision and its reasoning, and a reason that still matters is
kept as a rejected alternative in the context. A new entry is written only for a
new topic. Entries describe the current decision rather than how it was reached.
Concrete type shapes belong here.

`docs/ideas/`, committed, holds one file per idea, named in kebab-case rather
than numbered, because ideas get promoted and dropped and their order means
nothing. Each file carries a status line, either `idea`, `planned`, or `built`
with a link to the entry that settled it, then four sections: what it does, why
it is interesting, what it would touch, and open questions. A README indexes
them. An adopted idea is settled in the entry for its topic, or in a new entry
if it is a new topic, and keeps its file.

The work plan, under the gitignored `docs/superpowers/plans/`, tracks where the
work actually stands. It is disposable.

`README.md` keeps its existing audience, a visitor to the repository.

## Consequences

`DESIGN.md` describes a target the code does not meet during a rebuild. One line
at the top points at the work plan and is deleted when the gap closes.

`AGENTS.md` becomes mechanical: build, run, formatting, hooks, layout. It gains
a section saying which document to read and when.

Four documents have to stay consistent with each other, and the split creates
the drift it is designed to survive. A changed decision means editing its entry
and `DESIGN.md`, and missing the second is the common failure.

Rewriting in place means the log does not show what changed. `git log` on an
entry does.

An idea has somewhere to go the moment it appears, so a conversation can end in
neither a decision nor a loss. The failure mode is an idea that was built and
still says `idea`. The list is committed, so a visitor reads it as a roadmap. It
is not one, which its README says.

The work plan does not travel between machines, being gitignored, so anyone
picking the work up elsewhere gets the design and the reasoning and has to be
told separately where the work stands.
