# Page frame rate

Status: idea.

## What it does

Measures how smoothly the page draws a busy world and fixes what slows it, so a
world of 200 creatures plays at 60 frames a second on an ordinary laptop.

## Why it is interesting

Roadmap entry 14 brings new art and motion: hops, a leaping wolf, shared tiles
and markings drawn from genes. Each costs drawing time, and a page that stutters
undoes the point of making it look good. The entry promised the 60-frame check
and left it out, so the check gets the focus of its own pass.

## What it would touch

The drawing code in `frontend/src/scene.ts` and the modules behind it, plus the
written steps for a check anyone can repeat.

The cheapest check found in the grill takes about three minutes by hand. Spawn
180 rabbits and 20 wolves in one command, open Chrome's frame meter (DevTools,
"Show frame rendering stats"), set the CPU to 4x slowdown as a stand-in for a
laptop, and play 30 seconds at 2 days a second and 30 at 10. A script in
`tools/` driving a headless browser would repeat it unattended, but a headless
browser renders differently from a real one.

## Open questions

Which machine stands in for an ordinary laptop: a real one, or a desktop with
the CPU slowed.

Whether the check becomes a script once it is run often enough to be worth it.

Whether it comes before or after hosting, which multiplies viewers but not
drawing cost.
