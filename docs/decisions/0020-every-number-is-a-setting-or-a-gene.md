# 0020. Every number is a setting or a gene

## Status

Accepted, 2026-09-27. Amends
[0003](0003-entities-carry-components.md) and
[0006](0006-genomes-are-a-named-layout-of-gene-shapes.md).

## Context

The simulation is meant to be configurable throughout, with defaults that work
and nothing that has to be edited before a run is watchable. Today most numbers
that govern behaviour are literals inside behaviour classes, and the ones that
are configurable live in a flat table of world parameters wired to a hand-written
panel, so adding one means editing the interface.

[0003](0003-entities-carry-components.md) gave a species a `Map<String, Double>`
of settings, which is enough to hold a value and not enough to render an editor
for it or to keep a value in range.

Typed configuration records per species were the alternative. A typo would not
compile, and every species would need its own panel, so adding a setting would
touch the interface again.

## Decision

Every number a rule reads is either a gene or a setting. It is a gene when it
varies between individuals and passes to a child, per
[0006](0006-genomes-are-a-named-layout-of-gene-shapes.md), and a setting
otherwise. A literal in a system is a bug.

A setting is declared rather than merely stored, and the declaration carries what
the interface needs to edit it. `Setting` is sealed, and its shapes are added when
something needs one, starting with a bounded scalar rendered as a slider, a choice
from a fixed set, and a toggle.

Every setting has a default, so a species and a world are usable without
configuring anything.

Settings are read live from the species, so an edit reaches every creature
already alive, as the 2022 panel does. A `Spawn` carries its species rather than a
copy of its values.

The parameters panel is generated from the declarations, so a new setting appears
in the interface with no interface code. Editing one is a command, per
[0019](0019-commands-queue-at-the-day-boundary.md).

## Consequences

[0003](0003-entities-carry-components.md)'s settings map becomes a declared,
typed set, and the flat reserve of zero for a newborn in
[0006](0006-genomes-are-a-named-layout-of-gene-shapes.md) becomes the default of
a setting.

A world has settings too, such as grid size and how much food arrives, and they
are declared the same way.

Keys are strings, so a typo is a runtime failure, which this log already accepts
for genes and for perception tags.

Bounds keep a value in range and say nothing about whether it is sensible.
Nothing stops a configuration that makes the world dull or unsurvivable, which is
the point of being able to edit it.

A generated panel is plainer than a hand-made one, and it is the only kind that
cannot fall behind the model.

Because commands drain at the day boundary, a live read is stable within a day.
