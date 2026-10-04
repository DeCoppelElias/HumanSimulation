# 0017. Every number is a setting or a gene

## Status

Accepted, 2026-09-27. Revised 2026-10-04.

## Context

The simulation is meant to be configurable throughout, with defaults that work
and nothing that has to be edited before a run is watchable. Today most numbers
that govern behaviour are literals inside behaviour classes, and the ones that
are configurable live in a flat table of world parameters wired to a
hand-written panel, so adding one means editing the interface.

A plain map of doubles per species was the smallest option. It holds a value and
cannot render an editor for it or keep it in range.

Typed configuration records per species were the other alternative. A typo would
not compile, and every species would need its own panel, so adding a setting
would touch the interface again.

A world's dimensions and its starting population were once meant to be world
settings. The world reads its dimensions only when it is built, so editing them
on a living world would need a rule that no other setting has, that the edit
waits for a reset. And the starting population is input to a run rather than a
rule the world reads. Both became inputs instead, the dimensions to the world's
constructor and the starting population as spawn commands an adapter sends, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md).

## Decision

Every number a rule reads is either a gene or a setting. It is a gene when it
varies between individuals and passes to a child, per
[0006](0006-genomes-are-a-named-layout-of-gene-shapes.md), and a setting
otherwise. The one other kind of number is a gene's own bounds and mutation
size, which are declared in the species' gene layout, so they are part of the
same recipe and not literals either. A literal in a system is a bug.

A setting is declared rather than merely stored, and the declaration carries
what the interface needs to edit it. `Setting` is sealed, and its shapes are
added when something needs one, starting with a bounded scalar rendered as a
slider, a choice from a fixed set, and a toggle.

Every setting has a default, so a species and a world are usable without
configuring anything.

Settings are read live from the species, so an edit reaches every creature
already alive, as the 2022 panel does. A `Spawn` carries its species rather than
a copy of its values, per [0003](0003-entities-carry-components.md).

The parameters panel is generated from the declarations, so a new setting
appears in the interface with no interface code. Editing one is a command, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md).

## Consequences

The breeding interval, the breeding cost, the endowment fraction, the eating
charge and the charge for speed are all species settings.

A world has settings too, such as how much food arrives and how often, and they
are declared the same way.

Keys are strings, so a typo is a runtime failure, which this log already accepts
for genes.

Bounds keep a value in range and say nothing about whether it is sensible.
Nothing stops a configuration that makes the world dull or unsurvivable, which
is the point of being able to edit it.

A generated panel is plainer than a hand-made one, and it is the only kind that
cannot fall behind the model.

Because commands drain at the day boundary, a live read is stable within a day.
