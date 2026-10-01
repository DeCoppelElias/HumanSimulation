# Humans

Status: idea.

## What it does

Adds the human as a species once rabbits and wolves are working. It is the one
creature the 2022 model has, and the one the project is named for.

## Why it is interesting

A human is too difficult to start with, per
[0002](../decisions/0002-replace-the-model-layer-in-place.md), and that
difficulty is the point. Whatever makes it harder than a rabbit is a capability
the world does not have yet, and each one should arrive as a component or a
system that rabbits and wolves can meet too.

## What it would touch

A species value, and the 2022 `Human.png` sprite, which is already at the
classpath root. Past that it depends on what a human can do that the other
species cannot.

## Open questions

What a human does that a rabbit and a wolf do not, which is what made it too
difficult to start with.

What its [diet](diet.md) accepts.

Whether a wolf hunts it, and whether it hunts anything.
