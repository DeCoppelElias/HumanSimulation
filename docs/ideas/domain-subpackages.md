# Domain subpackages

Status: idea.

## What it does

Splits the flat `domain` package into subpackages by topic, such as
`domain.grid`, `domain.genome` and `domain.day`, once the flat package has
grown hard to find your way around.

## Why it is interesting

A package of forty types reads as a heap, and grouping them by the words in
`DESIGN.md` would make the ubiquitous language visible in the tree. An agent
looking for where geometry lives would find a folder rather than search.

## What it would touch

Visibility, more than file locations. The flat package keeps the world's
internals package-private, which is how the compiler holds the aggregate
boundary from [0003](../decisions/0003-entities-carry-components.md). Every
type that crosses into another subpackage has to become public, and then the
adapters can reach it too. A split that keeps the world, the entity store and
the day's steps together in one package and moves only standalone values out,
such as the genome and the grid's geometry, keeps most of that guarantee.

## Open questions

When the flat package becomes the problem. Roadmap entry 6, when mutation,
breeding and settings land, is the likely point.

Whether a module descriptor, or an architecture test along the lines of the
`jdeps` one, could hold the boundary instead of package-private visibility, so
the split costs nothing in encapsulation.
