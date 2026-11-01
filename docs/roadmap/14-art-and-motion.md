# 14. Art and motion

Status: done.

The first of three entries that serve the first goal in `DESIGN.md`. Pixel art
drawn for the project replaces the placeholder asset packs for every species and
the ground, per [0025](../decisions/0025-the-art-is-pixel-art-drawn-for-the-project.md),
and the smooth glide from entry 3 becomes a hop for rabbits and a walk or a leap
for wolves, per [0020](../decisions/0020-the-interface-is-a-web-page.md).
Entry 15 lays out the page around the world, and entry 16 adds effects.

The art is already drawn and committed in `art/sprites.py`, which writes the
PNGs under `art/png/`: the rabbit's frames, the wolf's six frames in profile, the
three grass tufts and the four ground tiles. The page draws them at the largest
whole scale its current layout allows, never a fractional one. The meadow
drifts through autumn patches of lush green, gold and straw, and a dimmed tile
of it above the top row keeps a hop there in view. The wolf is 24 by
18 and overhangs its tile by 4 pixels on each side, and a click still goes by the
tile underneath.

A rabbit covers a move of any length in one bound, crouch, tucked air frame and
crouch, with an arc that rises with distance. A wolf walks a move of one tile
and leaps a longer one or a kill. A move takes 260 ms plus 160 ms a tile, cut to
90% of the day when the day is shorter, and when that leaves under 150 ms
creatures glide in their standing pose instead. A paused single step plays the
move at full length. Across a jump of more than one day nothing moves and the
fades still run. Both species blink while idle.

Up to three creatures on one tile are drawn, nudged a few pixels apart so they
overlap as a group, with a count chip whenever more than one is there, shown
once the day's moves have landed. A wolf
draws in front of a rabbit, and grass stays under both.

A rabbit's markings come from its genes, so similar genomes look alike. Each
gene is scaled by its range and measured from the species' baseline, and three
fixed blends pick a colour, how far a patch spreads (none, tail, tail and
haunch, or a full dutch coat) and an accent (none, forehead or chest). The page
computes them, and needs each species' gene ranges and baseline genome for it,
which the API does not send yet. The wolf marks in shades of
one of six coats, a darker saddle and a paler muzzle or legs, shown to Elias
before it was built. The rabbit's
grids gain a letter per marking region in place of the single second coat
colour.

The old sprites, their credits in the page's footer and
`frontend/public/sprites/LICENSE.txt` go, and `README.md` credits only what is
left. Nothing in the model changes, so seeded runs, the gates and the sample in
`README.md` stay as they are.

Vitest covers the move timing and its cap, the switch to a glide, the hop's
phases by distance, the wolf's walk or leap, the shared-tile offsets and chip,
when the chip shows, the meadow's patches and dither, and the marking blends
against fixed genomes, all as pure modules under `frontend/src/`.

On the page: rabbits bound and wolves walk and leap across the meadow in the
new art, a crowded tile shows its group and count, rabbits with similar genes
look alike, and at 10 days a second everything glides. The entry is done when
Elias has watched it and approved it.

Depends on entry 13. See
[0020](../decisions/0020-the-interface-is-a-web-page.md),
[0025](../decisions/0025-the-art-is-pixel-art-drawn-for-the-project.md).
