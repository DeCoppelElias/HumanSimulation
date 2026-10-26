# 9. Rabbit learns

Status: not started.

A gate rather than a feature: selection has to be shown working before a second
species muddies it. Rabbits that evolved compete against the genomes they
started from, and the world judges which lives better, so no gene is expected
to move any particular way.

`run` exports the genomes alive on given days and spawns a world from a file of
exported genomes, the part of `docs/ideas/brain-library.md` an experiment
needs. Saving a library, versioning layouts and loading genomes on the page
wait. 0021 is rewritten to describe the new flags before they are built.

A tool in `tools/` runs an ancestor competition, per
`docs/ideas/ancestor-competition.md`. Mutating worlds evolve and export their
survivors. Then, with mutation off, evolved and ancestral genomes share a
world, and each group also runs alone on the same seeds. The report gives the
evolved group's share of the shared world and each group's population alone,
with the statistics from entry 7.

Details to settle in this entry's grill: the world evolution runs in, including
the starting genome and any ramp; the world the test runs in; how many genomes
each group starts with; the pass marks for the shared world and for each group
alone; how many seeds; the tuning budget; and what happens if the budget runs
out without the gate passing.

Acceptance, on the gate's report, with the numbers the grill sets:

- the evolved group takes over the shared world from the ancestors in most
  seeds;
- alone, the evolved group holds a population at least as large as the
  ancestors' alone.

Where the brain's genes drift is reported per seed with no pass mark. Elias
reads it before the entry is done, so a gate passed only on cheaper bodies, or
on behaviour he does not want, is caught by a person rather than a threshold.
The network brain waits, per `DESIGN.md`.

On the page: rabbits heading for food more directly after a few hundred days
than at the start, if the grill keeps the poor starting genome.

Depends on entry 8. See
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0012](../decisions/0012-tests-target-brains-without-a-world.md),
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md),
[0023](../decisions/0023-rabbits-score-the-tiles-they-can-reach.md),
`docs/ideas/ancestor-competition.md`, `docs/ideas/brain-library.md`,
`docs/ideas/mutation-shadow.md`.
