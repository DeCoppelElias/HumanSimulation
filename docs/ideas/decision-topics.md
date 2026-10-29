# Decision topics

Status: idea.

## What it does

Regroups `docs/decisions/` into a handful of topic files, each named by a slug
such as `the-day.md`, and holds every section in them to a bar for being there
at all: the choice is hard to reverse, surprising to someone reading the code,
and the result of a real trade-off. What fails the bar moves into `DESIGN.md`,
or goes where the tests already show the choice. What passes is cut to the
decision, the alternatives worth remembering and the costs that are not
obvious.

A first grouping, to be settled when the idea is promoted:

- how the work is organised:
  [0001](../decisions/0001-documentation-splits-into-five-artifacts.md),
  [0002](../decisions/0002-replace-the-model-layer-in-place.md),
  [0019](../decisions/0019-workflows-are-skills-and-invariants-are-scripts.md);
- build and test standards:
  [0012](../decisions/0012-tests-target-brains-without-a-world.md),
  [0022](../decisions/0022-warnings-fail-the-build-and-nothing-is-null.md);
- the world, its entities, terrain and space:
  [0003](../decisions/0003-entities-carry-components.md),
  [0009](../decisions/0009-terrain-is-entities-plus-tile-fields.md),
  [0011](../decisions/0011-traversal-goes-through-queries.md),
  [0015](../decisions/0015-geometry-lives-in-the-grid.md),
  [0024](../decisions/0024-a-part-can-end-its-carrier.md);
- the day:
  [0004](../decisions/0004-creatures-return-intents.md),
  [0005](../decisions/0005-a-day-is-one-method.md),
  [0008](../decisions/0008-brains-decide-systems-apply.md),
  [0010](../decisions/0010-runs-replay-exactly-from-a-seed.md),
  [0014](../decisions/0014-resolve-order-comes-from-a-speed-gene.md),
  [0016](../decisions/0016-feeding-takes-the-tile.md);
- brains and perception:
  [0013](../decisions/0013-perception-is-one-type.md),
  [0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
  [0023](../decisions/0023-creatures-score-the-tiles-they-can-reach.md);
- genes and settings:
  [0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md),
  [0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md);
- the boundary, its state, interface and agents:
  [0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
  [0020](../decisions/0020-the-interface-is-a-web-page.md),
  [0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).

## Why it is interesting

The log is read before work on its topic, so its length is paid on every
roadmap entry, and every entry is something `DESIGN.md` has to agree with.
Fewer and shorter topics mean less to read and less to keep in step. Related
choices, such as the steps of a day and the order they resolve in, are read
together instead of followed across several entries.

An entry that outlives its reason is worse than none. An agent reads an
accepted entry as binding and holds the code to it.

To keep it that way, `tools/docs-check.py` fails when the log holds more topic
files than a limit written in its README, so a new topic is an edit to that
limit that shows up in review. The bar becomes the first step of the
changing-decisions skill.

## What it would touch

Every file in `docs/decisions/` and its README, `DESIGN.md` where content moves
into it, every document that links a decision, `tools/docs-check.py` and its
tests, and the changing-decisions and auditing-docs skills with their `evals/`
scenarios. It is best done together with
[stable references](stable-references.md), since both rename the decision
files.

## Open questions

Which sections fail the bar. Titles mislead: some read as code structure and
turn out to weigh alternatives that were really rejected, so each one is read
rather than sorted by title.

Where the groups are drawn, and what the limit on topics starts at.

Whether a merged topic gets too long to read for one question, and whether
headings inside it are enough to fix that.
