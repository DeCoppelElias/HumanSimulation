# 0025. The art is pixel art drawn for the project

## Status

Accepted, 2026-10-31.

## Context

The first sprites came from two free packs, a CC0 rabbit and grass from Kenney
and a CC BY 4.0 wolf from another artist, in two styles that do not match. The
first goal in `DESIGN.md` asks for a world people watch for its own sake, and
every species after the wolf would need a third pack in a third style.

Paid packs were the first alternative, with one artist's set covering many
animals. The repository is public, and committing a paid pack's PNGs
republishes them, which their licences forbid. Art Elias draws himself stays
the fallback if drawn art does not clear the bar, along with paid art kept out
of the repository.

Elias compared today's sprites with a pixel-art draft side by side and picked
the pixel art. Sprites of 8, 24 and 32 pixels were drawn beside 16, and he
picked 16.

The wolf was first drawn facing the viewer, after a chibi reference. Its walk
cycles did not read, and Elias sent side-on references, so it is drawn in
profile.

Four ways to tell rabbits apart were weighed: all white, a coat chosen by id, a
coat gene inherited like any other, and a coat that shows one gene. Elias
proposed markings that follow the whole genome, so relatives and similar
brains look alike. Small spots were tried first and could not be read at 16
pixels, so the markings are regions of the coat.

Several creatures on one tile were drawn first as one sprite with a count. A
group of up to three sprites, nudged a few pixels apart, shows the crowd, and the
count chip shows on every shared tile so a group never hides a fourth.

## Decision

Claude draws the art as pixel art, following reference images Elias supplies
for each species, and Elias approves each pass in a published artifact before it
is built. How a sprite is drawn and turned into the page's PNGs is in
`AGENTS.md`.

A tile is 16 by 16 art pixels. A creature may be larger than its tile and
overhang it, as the wolf does at 24 by 18, and a click still goes by the tile
underneath. The page scales the art by whole numbers only, so every art pixel
stays square and sharp.

The style is flat colour with a navy ink outline one pixel wide. Animals are
drawn side-on, and grass and ground are a bright top-down meadow, with ground
tiles in a few variants and tufts in a few shapes, each picked per tile.

A creature's markings come from its genes. Each gene is scaled by its range and
measured from the species' baseline, and fixed blends of those values pick a
colour, how far a patch spreads and an accent. The page computes them, so the
model knows nothing about them.

Up to three creatures on one tile are drawn as a group, with a count chip
whenever there is more than one. A predator draws in front of its prey, and
grass stays under both.

## Consequences

The art is free of licences the project cannot meet, and a new species costs
a drawing pass with references rather than a search for a matching pack.

Drawn art depends on the eye of whoever approves it. A pass that does not clear
the bar falls back to art Elias draws or paid art, which would live outside the
repository.

The markings change when a species' genes change, since they are computed from
the gene ranges and the baseline.
