# Remove command

Status: idea.

## What it does

A command that removes an entity by id, so a creature or a patch of food can be
taken out of a running world by hand. The 2022 interface had no such button.

## Why it is interesting

It makes a world easy to set up for a test, such as thinning out rabbits to see
what a wolf does with fewer of them. It is also the first command that names an
id, which gives the rule in
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md) that an
id which no longer resolves leaves the world unchanged a command to test it
through.

## What it would touch

One command type and its validation. On the page it pairs with selecting a
creature, which arrives in roadmap entry 9, or with clicking a tile in entry 3.

## Open questions

Whether removing by clicking a tile takes every occupant or only the one drawn.
