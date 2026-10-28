# Shared workflow kit

Status: idea.

## What it does

Moves the parts of the workflow that are not about this project into a
repository of their own, which other projects install: the document layout, the
generic skills, `tools/docs-check.py`, `tools/comment-check.py` and the skills'
`evals/` scenarios. This project keeps a pinned install and its own settings,
such as its paths, its build command and whose approval the skills wait for.

## Why it is interesting

The workflow was copied from project to project, and each copy changed a
little, so the older projects run older versions of it. A kit changes in one
place and every project picks the change up deliberately.

To keep it that way, the project holds no copy of the kit, only the install
and its settings, so there is nothing left to drift.

## What it would touch

The generic skills and the two scripts leave this repository. `AGENTS.md`,
[0001](../decisions/0001-documentation-splits-into-five-artifacts.md) and
[0019](../decisions/0019-workflows-are-skills-and-invariants-are-scripts.md)
point at the kit, and CI installs it so the docs check still runs. It comes
after the [skill review](skill-review.md), so the kit starts from skills that
have been checked.

## Open questions

Whether it is a plugin, which updates in one place, or a template repository,
which is copied and drifts again.

How a skill that names a decision, a build command or Elias gets those from
the project's settings.

Where the scenarios run once the skills live elsewhere.

Whether to wait until a second project wants the kit, which is the
recommendation. Until then it is one project's workflow with extra packaging.
