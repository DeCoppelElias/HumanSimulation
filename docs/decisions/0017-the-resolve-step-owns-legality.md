# 0017. The resolve step owns legality

## Status

Accepted, 2026-09-27.

## Context

[0004](0004-creatures-return-intents.md) moved legality out of the creature and
into one place, and said nothing about what legal is. The rules below have no
home anywhere else, so they were being answered by whoever wrote the first
system.

## Decision

A tile holds any number of entities, as the 2022 model does. Movement is blocked
only by an entity carrying a blocking component, so crowding is felt through food
running out rather than through space.

A blocked mover walks as far as it legally can and stops there, restating
[0004](0004-creatures-return-intents.md).

An entity removed earlier in the same step is skipped when its turn arrives, and
an attack naming a target that is gone resolves as nothing. The body stays in the
world until the clean-up step, so it is still perceived and still occupies its
tile for the rest of the day.

Breeding takes its interval, its cost and the child's starting reserve from
species settings, per
[0020](0020-every-number-is-a-setting-or-a-gene.md). A parent breeds when the
cooldown has elapsed and the reserve covers the cost.

Attacking arrives with the combat slice and not before. Two things about it are
settled: how far a creature can strike is a component, so reach belongs to the
creature rather than to the resolver, and an attack is a contest both sides can
lose. What it rolls against, whether reach is a gene or a setting, and what a
kill yields are left open until there is a predator to ask.

## Consequences

Many creatures on one tile is normal, so the interface has to show a stack where
it currently draws one sprite. That is `docs/ideas/drawing-a-stacked-tile.md`.

A child is always born somewhere, since a tile cannot be full.

Nothing dead acts, which makes being early a real advantage and gives the speed
gene from [0014](0014-resolve-order-comes-from-a-speed-gene.md) something to buy
once combat exists.

A day can contain fewer actions than it collected intents, so anything counting
actions has to read what was applied rather than what was decided.

Deferring the attack means the sealed intent type gains a case later, and the
switch in the resolver fails to compile until that case is handled, which is the
reminder that nothing else provides.
