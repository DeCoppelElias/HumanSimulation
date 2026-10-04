# 0002. Replace the model layer in place, growing from zero

## Status

Accepted, 2026-09-03. Revised 2026-10-04.

## Context

The model layer cannot grow. The world knows every content type by name and
branches on it in a dozen places, a creature's life is a fixed script, boundary
rules live inside the agent, and the genome is constructor parameters. Adding a
wolf means editing the world, the tile, the renderer and three behaviour
classes.

Growing the new core in a package beside the old one was the alternative, and it
had two arguments. A headless runner could measure both cores on the same seed.
And the nine regression scenarios could be ported one at a time and checked
against a version that still demonstrably passes them, so there would always be
a working oracle.

Neither holds. The comparison is not wanted, since a before and after gif is
enough. And there is no porting. Growing from zero means each rule arrives with
a fresh test written against the new units, so the old suite is never the thing
being satisfied and an oracle has nothing to do.

The first species could have been the human, the one creature the 2022 model
has and the one the project is named for. A human is too difficult a creature
to start with. A rabbit that wanders, eats grass and breeds asks nothing of the
first packages that they are not already building, and it is the natural prey
for the wolf that tests the design.

Keeping part of the 2022 code was the earlier plan: `GridPosition`, the line
chart, most of the Swing panels, and the determinism, resource and geometry
suites. It lost on two counts. The 2022 code is worth keeping for what it does
rather than how it is written, and its package names and idioms predate the
standards the rebuild follows. And the interface moved to the web in
[0020](0020-the-interface-is-a-web-page.md), which leaves the panels and the
chart nothing to attach to.

Rebuilding on one long-lived `rebuild` branch, merged to `master` at roadmap
entry 6, was the plan until entry 1 was about to start. It kept a working
application on `master` for anyone who cloned it. Nobody does: there are no
other collaborators, and the 2022 version stays reachable on the
`v1.0-original-2022` tag and the `original-2022` branch. What the branch cost
was real. CI built only `master` and pull requests, so pushes to it went
unbuilt, the roadmap and skills had to explain which branch to use, and it had
already drifted from `master` before any code landed.

A pull request per entry was the alternative to merging locally. CI would
build the branch on both platforms before it reached `master`. Elias works
alone and prefers speed, so a failure on the platform not tested locally is
fixed on `master` after the push instead.

## Decision

Replace in place on `master`. Each roadmap entry is built on its own
short-lived branch, named `entry-N-slug`, and merged into `master` locally once
it is finished, reviewed and Elias has said go. `master` always holds the last
finished entry, never half of one.

Build up from zero rather than porting the current model wholesale. The first
package is thin and complete: a grid, one species, which is the rabbit, a random
brain, a move intent, a resolver, a snapshot, and the `run` command printing
its census. The second serves it over HTTP and the third puts it on the page.
Capabilities are added one at a time after that, each package ending with
something watchable. The first two are the exception, watched as data, because
the domain and the whole web stack together would be a slice too large to
finish in one go.

No code survives. The 2022 version is a reference for features, and the first
commit of roadmap entry 1 deletes it with its tests. Each 2022 feature is an
acceptance line on the roadmap entry that rebuilds it, and a feature dropped on
purpose is in `docs/ideas/`. The rules the carried-over suites guarded, that a
seeded run replays and that positions measure distance correctly, get fresh
tests against the new types.

## Consequences

Git provides the reference, since the old core is one checkout away and the 2022
original is on `v1.0-original-2022`. What it does not provide is two cores
running in one process on one seed, so any comparison is a matter of running
one, recording numbers, and running the other.

Eight of the nine model-bug regression rules become acceptance criteria on the
package that reintroduces each rule, per
[0012](0012-tests-target-brains-without-a-world.md). The metabolism package, for
example, carries the interval validation.

Behaviour rules that were never bugs need the same treatment and are easier to
lose, since nothing in the tree names them. The roadmap owns that list, as
acceptance lines on each entry. A rule dropped on purpose goes to `docs/ideas/`
so nobody restores it thinking it went missing, as the 2022 food contest has.

Each package is a live test of the claim in `DESIGN.md`.

The rabbit's sprite comes from a CC0 asset pack until a dedicated art pass. The
project keeps its name while its first species is not a human, and humans wait
in `docs/ideas/humans.md`.

`master` has no graphical interface from entry 1 until the browser arrives in
entry 3, only the `run` command. The README says so, and the 2022 application
is one tag away. Growing vertically keeps the window short.

CI runs on the push to `master`, after the merge, so a failure shows there
rather than before it lands.
