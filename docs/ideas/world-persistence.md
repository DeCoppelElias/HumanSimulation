# World persistence

Status: idea.

## What it does

Keeps a server's worlds across a restart. Each world's seed and command log are
written to disk as they grow, and a starting server replays them, so closing
the terminal, a crash or a reboot no longer loses a world someone was watching.

## Why it is interesting

A person's worlds are never deleted while `serve` runs, so that they can always
go back to one. Persistence makes that hold across restarts too, which matters
most for a world left to evolve for days.

## What it would touch

The web adapter writes and reads the files. Replay already exists, per
[0010](../decisions/0010-runs-replay-exactly-from-a-seed.md), so the domain
changes nothing. It does turn the command log into a save format, which
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md) resists,
so it needs that entry revised first.

## Open questions

Replaying a long run at startup takes as long as running it did without the
playback delay. A world thousands of days old may want snapshots of its state
on disk instead of, or beside, its log.

Whether the files carry a format version, and what happens to a saved world
after a rule changes and its log no longer replays to the same place.
