# Roadmap

The order the project is being rebuilt in. `DESIGN.md` describes the target
architecture; this says which parts of it exist yet and what's next. Status
lives on each entry rather than as a separate description of what the code
contains, which would duplicate the code and go stale the same way. Read
`docs/decisions/` alongside this: a decision explains why something is shaped
the way it is, an entry says whether it exists yet.

Each entry is one file in this folder. Its number is a permanent id, like a
decision's, and is never reused. The list below is the build order, and an
entry's "Depends on" line says what has to come before it. Adding an entry is a
new file with the next free number and a line in the list. Dropping one deletes
its file and its line, and a dropped entry's idea goes back to `docs/ideas/`.

Each entry is built on its own branch and merged into `master` when it is
finished, per [0002](../decisions/0002-replace-the-model-layer-in-place.md), so
`master` holds the last finished entry. The 2022 application stays on the
`v1.0-original-2022` tag. Its inspection features wait for entry 9.

Each entry lists the 2022 features it brings back and the regression rules it
carries, as acceptance lines. A 2022 feature left out on purpose is in
`docs/ideas/`.

- [1. Minimal loop](01-minimal-loop.md)
- [2. Serving worlds over HTTP](02-serving-worlds-over-http.md)
- [3. Watching in a browser](03-watching-in-a-browser.md)
- [5. Eating and starving](05-eating-and-starving.md)
- [6. Breeding and settings](06-breeding-and-settings.md)
- [7. Rabbit learns](07-rabbit-learns.md)
- [8. Predator](08-predator.md)
- [9. Inspecting creatures](09-inspecting-creatures.md)
- [10. Looking good](10-looking-good.md)
- [11. Hosting](11-hosting.md)
