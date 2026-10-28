# Stable references

Status: idea.

## What it does

Names roadmap entries by slug, such as `predator.md`, and keeps the build order
only in `docs/roadmap/README.md`. Ideas and roadmap entries link the decision
topic they depend on rather than restating what it says. No document quotes a
fact that changes with the tree, such as a line count, a commit count or which
entry something comes after.

## Why it is interesting

A number that is both an entry's name and its place means an insertion renames
the files after it and rewrites every reference to them. A restated decision
goes wrong the day the decision changes, and nothing notices, since the link
still resolves. A quoted count is stale on the next commit.

The rejected alternative in
[0001](../decisions/0001-documentation-splits-into-five-artifacts.md) was a
permanent id, because it stopped saying when an entry comes. A slug never
claims to, so a reader takes the order from the list, which is where it is
kept anyway.

To keep it that way, `tools/docs-check.py` fails on an `entry N` reference or a
bare decision number in `docs/`, and the auditing-docs skill gains a pair to
read: whether an idea or entry repeats what a topic it links says.

## What it would touch

The roadmap files and their README, the numbering rule in
[0001](../decisions/0001-documentation-splits-into-five-artifacts.md), every
idea and entry that cites a number or restates a decision,
`tools/docs-check.py` and its tests, and the promoting-ideas and auditing-docs
skills with their `evals/` scenarios. The `planned` status line format in the
ideas README would link the entry instead of naming its number. It is best
done together with [decision topics](decision-topics.md).

## Open questions

What finished entries keep. Commit messages and branch names cite their
numbers and cannot be edited, so finished entries may keep numbered names while
new ones are slugs.

Whether entry branches become `entry-slug`.

How strict the restating rule is. Naming what a topic decided in a clause is
often what makes an idea readable, so the line is between pointing at a
decision and copying it.
