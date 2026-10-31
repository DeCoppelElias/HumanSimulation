# Showing grass age

Status: idea.

## What it does

Draws a grass tuft that yellows over its last few days, so a watcher sees which
grass is about to wither. Grass lies on its tile until it is eaten or its
lifespan runs out, 12 days by default.

## Why it is interesting

Food running out is what limits the rabbits, and today a tuft looks the same on
its first day and its last. A yellowing tuft shows where food is about to
disappear before it does.

## What it would touch

The art in `art/sprites.py`, which gains one or two older tufts, and the page,
which picks a tuft by age. The page needs each grass's age in the snapshot,
which it may already carry through the entity's info.

Roadmap entry 14 left it out to keep one tuft and a simple page.

## Open questions

How many stages, and from which day: a fixed number of days before the end, or
a share of the lifespan setting.

Whether withering on the last day gets an effect, the way a death does.
