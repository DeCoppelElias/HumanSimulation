# 0017. Every number is a setting or a gene

## Status

Accepted, 2026-09-27. Revised 2026-10-15.

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
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md). A flag marking a
setting as settable only at creation would have kept them declared, and it
needs a reset that waits for edits and rebuilds the grid, for two numbers.

Settings were first read live from the species. Every world on a server holds
the same species objects, so an edit in one world reached every other. Building
fresh species for each world kept them apart, but made a species a mutable
value, and isolation then depended on every adapter and test remembering to
build its own.

A reset could revert edited values to their defaults, which leaves the world
exactly as building it would and loses the tuning that is usually why someone
resets. Keeping the edits without logging them breaks replay from the seed and
the log, per [0010](0010-runs-replay-exactly-from-a-seed.md).

The page could restore defaults by sending each default back as an edit. It
would then restore what it believes the defaults are, which a tab opened before
a rebuild gets wrong, and the log would show edits where someone restored.

Declarations could travel in every snapshot, which repeats their descriptions
every day, or behind an endpoint with a stream event when values change, which
is a second way for state to leave the world.

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

A species or a world process owns its declarations and their defaults, and
they stay an immutable recipe. The world owns a table of current values, filled
from the defaults when it is built. A component reads its values live through a
handle the `Spawn` gives it, so an edit reaches every creature already alive, as
the 2022 panel does, and only in its own world. A process reads its values the
same way.

Values are grouped by owner: `world` for every process's settings, or a species
name. A world refuses a species named `world`, and two processes declaring one
key, when it is built.

Two commands change the table, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md):

```java
record SetSetting(String owner, String key, double value) implements Command {}
record RestoreDefaults(Optional<String> owner, Optional<String> key) implements Command {}
```

`SetSetting` is checked against the declaration's bounds and step.
`RestoreDefaults` copies the declared defaults back: with no owner the whole
table, with an owner that group, with both one setting. A key without an owner
is refused when it is built. A world created with values of its own gets them
as `SetSetting` commands on day 0, like its spawns.

A reset keeps edited values, and writes each value that differs from its
default into the new log as a `SetSetting` right after the reset, so the seed
and the log still describe the run.

No setting is settable only at creation. Width, height and seed are constructor
inputs. A `Setting` gains a flag for that the day one needs it.

The current values leave in the snapshot, grouped by owner, since they are world
state. The declarations never change for a world, so the web adapter serves
them once, in panel order, the world first and then each species by name.

The parameters panel is generated from the declarations, so a new setting
appears in the interface with no interface code. It marks each setting whose
value differs from its default, with a restore button beside it and one for the
whole panel.

## Consequences

The breeding interval, `childReserve`, `breedingLoss`, `parentMargin`, the
eating charge and the charges for speed and view range are all species
settings.

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

Worlds on one server share species objects and never share an edit. Changing a
default is a code change, and no command can do it.

A logged `RestoreDefaults` replays to the defaults of the build replaying it, so
a log replayed on a build with other defaults diverges there. Replay is promised
within one build.
