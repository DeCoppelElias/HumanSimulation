# 0001. Documentation splits into three artifacts

## Status

Accepted, 2026-09-03.

## Context

Design notes for this project have so far been single-phase work products kept
outside git, with the durable record living in agent memory. Memory is a fine
backstop for six decisions and a bad one for a document, and a gitignored file
means every clone and every CI run has a different idea of what the project is.

Three kinds of content were getting mixed. Why the project exists changes rarely
and wholesale. A decision with its reasoning never changes at all, it gets
superseded. A description of the current tree is stale on the next commit. A
single document holding all three churns constantly and stops being trusted.

## Decision

Three artifacts with three jobs.

`DESIGN.md`, at the repository root and committed, describes the system as it is
meant to be. Present tense, no history, no progress, no alternatives. Its readers
are Elias and Claude. It holds vision, non-goals and architecture, and no code.

`docs/decisions/`, committed, holds this log. One entry per settled choice, in
Nygard format, append-only. Concrete type shapes belong here, because an entry is
a dated record and cannot go stale.

The work plan, under the gitignored `docs/superpowers/plans/`, tracks where the
work actually stands. It is disposable.

`README.md` keeps its existing audience, a visitor to the repository.

## Consequences

`DESIGN.md` describes a target the code does not meet during a rebuild. One line
at the top points at the work plan and is deleted when the gap closes.

`AGENTS.md` becomes mechanical: build, run, formatting, hooks, layout. It gains a
section saying which document to read and when.

Three documents have to stay consistent with each other forever, and the split
creates the drift it is designed to survive. A decision that changes now means
editing an entry's status, writing a new entry, and editing `DESIGN.md`, and
missing the third is the common failure.

The work plan does not travel between machines, being gitignored, so anyone
picking the work up elsewhere gets the design and the reasoning and has to be
told separately where the work stands.
