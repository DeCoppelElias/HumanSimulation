# 3. Watching in a browser

Status: done.

The frontend is React with TypeScript and Vite in `frontend/`, built into the
jar by `frontend-maven-plugin` through `target/classes/public`, so nothing it
generates is committed. `serve` opens it in the browser with `java.awt.Desktop`,
and logs the address where no desktop is available. The Vite dev server proxies
the API to a running `serve` for hot reload.

The build meets [0022](../decisions/0022-warnings-fail-the-build-and-nothing-is-null.md):
one exact Node version, exact package versions, a committed
`package-lock.json` installed with `npm ci`, and Dependabot for npm grouped
weekly. `./mvnw verify` runs strict `tsc`, ESLint with the hooks rules and no
warnings, Prettier's check and Vitest. `-Pskip-frontend` skips all of it for
Java-only work and is never used in CI or the release.
`tools/comment-check.py` covers `.ts` and `.tsx`.

A PixiJS canvas draws the grid with a rabbit sprite from a Kenney CC0 pack, with
its licence beside the file, on a flat green ground with faint tile lines. It diffs snapshots by id,
fades rabbits in and out, and shows a count on a tile holding more than one,
drawing the lowest-id occupant. A rabbit glides between its tiles only when the
day went up by exactly one, over one day at the playback rate, or 300 ms for a
step while paused. Across a bigger jump rabbits snap to their tiles. A snapshot
that arrives mid-glide finishes the glide at once. A snapshot whose seed changed
or whose day went backwards starts a new run, so nothing glides across a reset.

The controls are step, play and pause, and playback rate; spawning rabbits by
count and by clicking a tile; resetting the world; creating or resetting a world
sends the starting spawns, ten rabbits, since a world starts empty; an optional
seed beside create and reset, drawn from the clock when blank and shown; and a
fast-forward with a day count, 100 by default and at most 10,000, which on a
playing world pauses, steps and plays again, with the controls disabled until
it returns. A picker lists the server's worlds and switches, creates or deletes
them. The world's id is in the address as `/?world=ID`, so a reload returns to
it. With no id, or one the server does not hold, the page shows the picker, the
latter with a notice, and deleting the world on screen returns to it. Errors
show as a short notice. The population chart is uPlot, with a statistics
reset, and fetches only the days it is missing from the census history.

The stream gains the play state, per
[0020](../decisions/0020-the-interface-is-a-web-page.md): a `playState` event with
whether the world plays and its rate, sent when a viewer attaches and on every
change, including the idle pause and a day that fails. A viewer holds it apart
from the latest day. ApiTest covers it. A page closes its stream while its tab
is hidden and opens a new one when shown, catching up from the census history.

Vitest covers the frontend's diffing, gliding and fading, starting a new run,
and filling a gap in the chart.

2022 features that come back here: spawning by count and by click, stepping and
automatic play with a rate, resetting the world, and the population graph with
a statistics reset.

On the page: a plain ground of 20 by 20 tiles with ten rabbits that glide
between tiles when you press play, fade in when spawned, and show a count where
they share a tile, beside the population chart and the current seed.

Depends on entry 2. See
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
[0020](../decisions/0020-the-interface-is-a-web-page.md).
