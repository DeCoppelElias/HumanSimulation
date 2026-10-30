# Roadmap

The order the project is being rebuilt in. `DESIGN.md` describes the target
architecture; this says which parts of it exist yet and what's next. Status
lives on each entry rather than as a separate description of what the code
contains, which would duplicate the code and go stale the same way. Read
`docs/decisions/` alongside this: a decision explains why something is shaped
the way it is, an entry says whether it exists yet.

Each entry is one file in this folder, and its number is its place in the
build order, so the list below runs in ascending number. A finished entry keeps
its number for good. Adding, moving or dropping an entry renumbers the
unfinished entries after it, renaming their files and every "entry N"
reference to them, per
[0001](../decisions/0001-documentation-splits-into-five-artifacts.md). Entry 4
was dropped before that rule, and its hole stays. An entry's "Depends on" line
says what has to come before it. A dropped entry's idea goes back to
`docs/ideas/`.

Each entry is built on its own branch and merged into `master` when it is
finished, per [0002](../decisions/0002-replace-the-model-layer-in-place.md), so
`master` holds the last finished entry. The 2022 application stays on the
`v1.0-original-2022` tag.

Each entry lists the 2022 features it brings back and the regression rules it
carries, as acceptance lines. A 2022 feature left out on purpose is in
`docs/ideas/`.

- [1. Minimal loop](01-minimal-loop.md)
- [2. Serving worlds over HTTP](02-serving-worlds-over-http.md)
- [3. Watching in a browser](03-watching-in-a-browser.md)
- [5. Eating and starving](05-eating-and-starving.md)
- [6. Breeding and settings](06-breeding-and-settings.md)
- [7. Comparing runs](07-comparing-runs.md)
- [8. Entity lifespan](08-entity-lifespan.md)
- [9. Rabbit learns](09-rabbit-learns.md)
- [10. Diet](10-diet.md)
- [11. Predator](11-predator.md)
- [12. Fleeing](12-fleeing.md)
- [13. Inspecting creatures](13-inspecting-creatures.md)
- [14. Looking good](14-looking-good.md)
- [15. Hosting](15-hosting.md)
