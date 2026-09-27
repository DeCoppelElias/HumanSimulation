# 0019. Commands queue at the day boundary

## Status

Accepted, 2026-09-27. Extends
[0007](0007-state-leaves-as-a-whole-world-snapshot.md).

## Context

[0007](0007-state-leaves-as-a-whole-world-snapshot.md) made commands the only way
into the core and did not say when one lands.
[0010](0010-runs-replay-exactly-from-a-seed.md) promises that a run replays
exactly from a seed. A watched run takes clicks at arbitrary moments, so the seed
alone does not describe it, and a command applied between two systems breaks the
rule that every creature in a day sees the same world.

Applying a command the moment it arrives is the cheaper alternative and it makes
the interface feel instant. It also means the run someone actually watched, which
is the one they want to reproduce, is the one that cannot be.

## Decision

Commands queue, and the queue drains at the start of a day, before anything
decides. Each command is recorded with the day it applied on, so a run reproduces
from its seed plus its command log.

A command given to a paused world drains immediately and produces a new snapshot
without advancing the day, so clicking food onto the grid while paused shows the
food.

The world advances on the Swing event dispatch thread, where the previous phase
deliberately put it. A snapshot out and a queue in are exactly the handoff a
worker thread needs, so moving the world off the event thread stays available and
contained, and nothing needs it at a few hundred tiles.

## Consequences

A command is visibly applied on the next day. At a few days a second that reads
as immediate, and at one day every ten seconds it does not.

Reproducing a run means keeping the command log, so a bug report is a seed and a
list of commands rather than a seed.

The log is most of a save format, which is not a goal and is a thing to resist
growing.

Every operation the interface performs is a command type someone names, around
twenty of them per
[0007](0007-state-leaves-as-a-whole-world-snapshot.md).

A setting edited by command cannot change partway through a day, so a system
reading a setting live sees one value for the whole day.
