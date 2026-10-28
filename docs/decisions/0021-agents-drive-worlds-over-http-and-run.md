# 0021. Agents drive worlds over HTTP and through `run`

## Status

Accepted, 2026-10-01. Revised 2026-10-28.

## Context

An AI agent experimenting with the simulation is one of the two goals in
`DESIGN.md`. It needs to start worlds, change them, advance them and read the
results, and it runs commands as separate shell calls rather than holding a
session open.

Seven interfaces were weighed. A one-shot command-line run is the simplest and
suits batches, and it cannot be driven step by step. A command line that
rebuilds the world on every call from its seed and command log gives stepping
without a process, and it pays JVM startup on each call and turns the log into
a persisted format, which
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md) resists. A long-lived
process reading commands on stdin is fast per step and needs a background
process and a pipe, which is the worst fit for separate shell calls.

The HTTP API the web adapter already has is fast per call and shows the agent
exactly what the browser sees. Its weakness is the server's lifecycle: knowing
when it is ready, which port it took, and making sure it does not linger.

An MCP server over stdio lets the agent's client own that lifecycle and gives
typed tools. A study by Scale Labs found that current models succeed about as
often through MCP as through a command line, in fewer turns, that weaker models
did better through a command line on data-heavy tasks, and that offering both
interfaces did not help. It adds a dependency and a third adapter before
anyone knows interactive driving is needed often.

Experiments written as code against the domain are the most flexible and
stay available whenever the domain's public surface is kept small. A Python
bridge in the Gymnasium style serves reinforcement learning, which `DESIGN.md`
rules out.

How a server ends had several cases to cover: an agent driving a world step by
step, an agent leaving an experiment to run, a person clicking or only
watching, and a person letting a world play for a day and checking back. One
idle rule for every server either kills the world a person left playing
overnight or leaves a forgotten agent server running for good. Counting a
playing world as activity keeps the forgotten server alive whenever its last
world was left playing. Pausing worlds on idle saves CPU and stops the run an
agent deliberately left going. Splitting the rule by who starts the server
covers every case, since an agent's background process is invisible and a
person's runs in a terminal they can see.

Playing at a high rate and pausing on time was the alternative to skipping
ahead. It overshoots the day the agent meant to stop at, so an intervention
lands on a different day each time, and the agent has to poll to know when the
world got there. Raising the rate's ceiling to agent speeds would also let a
person's slider pick speeds no page can draw.

Comparing runs across many seeds was first meant to be a batch mode in `run`,
running the seeds in parallel in one process with a summary line per seed, and
overrides were meant to come from a file as well as the command line. A batch
saves JVM startup and warmup per seed, about half a second of a run that takes
three. A script that starts one `run` per seed in parallel gets most of that
speed, and it is where the comparison has to live anyway, since what to compare
changes faster than Java does. A file of overrides saves retyping a long command,
which a shell repeats for free, and tuned values become defaults in code rather
than a file to pass around. The file is kept in
`docs/ideas/run-settings-file.md`.

A comparison could also judge its result, such as calling a variant the winner
when it survives longer. That bakes one experiment's notion of success into a
tool every experiment shares, and it decides for the reader what the numbers
mean, which hides a result that passes for the wrong reason.

The comparison first reported only counts, such as how many seeds one variant
outlasted another. Over ten seeds a count cannot tell a real effect from luck: a
variant that truly wins 70% of the time reaches 8 of 10 only about 38% of the
time, and a tie counts as a loss. Cohen's d needs roughly normal values and has
none for a world still alive at the end. A bootstrap interval on the difference
of medians reads in real units and has the same trouble with a world still
alive. Survival analysis fits the day a species died out and nothing else.
Comparing every pair of variants grows the tests with the square of the
variants, and a correction across tests nobody asked for hides a real effect
against the control. Printing only corrected p-values hides the number a
measure chosen before the run should be judged by, and printing only raw ones
leaves a reader who scans the report to correct by hand.

An experiment that moves a population from one run into another needs the
genomes themselves, since a census mean describes a rabbit that never existed.
Writing them to stdout beside the census would make every reader sort two kinds
of line. One file per export day, named in the flag, is more files to keep
track of than one file with the day on every line. `run` could also choose
which genomes to spawn from a file, by day and count, drawing with the world's
generator. Which genomes an experiment wants changes faster than Java does, for
the same reason the comparison lives in a script, so `run` spawns every line
and the script chooses.

The ancestor competition could be a mode of the comparison script, or the
comparison script could learn to run any number of stages with files between
them. A mode changes the comparison from variants over seeds into stages, and a
general stage runner is a workflow engine for one experiment.

## Decision

The jar has three entry points. `serve` is the default and opens the browser.
`serve --no-browser` runs the same HTTP API with no page, for an agent. `run
--seed 42 --days 500` advances one world headless and prints its census as JSON
Lines, one object per day.

`run` takes `--days`, which is required, and `--seed`, which defaults to one
drawn from the clock. `--width` and `--height` default to 20. `--spawn
rabbit=10` gives a starting population, can be repeated once per species, and
defaults to ten rabbits, and any `--spawn` given replaces that default rather
than adding to it. Each line is a census row with the seed added, since a
comparison gathers many seeds' lines together and a line should stand on its
own:

    {"seed":42,"day":1,"population":{"grass":0,"rabbit":10},"geneMeans":{"rabbit":{"breedPull":1.01,"grassPull":1.03,...}}}

A species with no members, or no genes, has no entry under `geneMeans`.

The first line is day 1, since applying the starting spawns completes no day.

`serve` accepts `--port 0`, picks a free port, and prints one line on stdout
once it accepts requests, such as
`{"event":"ready","url":"http://127.0.0.1:54321","port":54321}`. Logging goes
to stderr, so stdout holds only machine-readable lines for both commands.
`--host` changes the address it listens on and `--max-worlds` the cap on
worlds.

The two modes end differently. `serve --no-browser` is an agent's and quits
after 10 minutes without a request or an open stream, and a playing world does
not keep it alive. `serve` with the browser may be a person's and never quits
or deletes a world: after 24 hours without a request or a stream it pauses any
playing world, which waits where it was. `--idle-minutes` adjusts either, and 0
turns it off. An agent closes a world it is finished with by deleting it, per
[0020](0020-the-interface-is-a-web-page.md), and has no way to stop the process
over HTTP.

An agent skips ahead by stepping a paused world a number of days at once, up to
10,000 per request, and gets the last day's report back. Every day's census row
stays in the history. That makes a perturbation experiment, such as running 500
days, adding wolves and running 500 more, a few requests that each stop at an
exact day. The playback rate stays bounded for watching, at 0.5 to 10 days a
second.

Worlds are addressed by id, so one server holds several, such as a control and a
variant side by side.

`run` overrides any declared setting with `--set owner.key=value`, repeatable,
sent as a `SetSetting` on day 0 and checked like an edit from the panel.
`--set-at DAY:owner.key=value` sends it once DAY days are complete, which is how
a world gets harsher partway through a run. `--set` is `--set-at 0`.

`--gene species.gene=value`, repeatable, overrides a gene of that species'
starting spawn, so an experiment can start a population from a worse genome
than its species' baseline. It rides on the `Spawn` command, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md), so the log still
describes the run. The HTTP API takes the same overrides as an optional `genes`
field on a spawn, and the page sends none.

`--export FILE` with `--export-day DAY`, repeatable, writes every creature with
genes that is alive once DAY days are complete to FILE, read from the day's
snapshot, per [0007](0007-state-leaves-as-a-snapshot-commands-go-in.md). A day
past `--days` is a usage error before the first day runs. The file is JSON
Lines, one creature per line:

    {"seed":7,"day":1000,"species":"rabbit","id":42,"genes":{"breedPull":1.2,"grassPull":1.4,...}}

`--spawn-from FILE`, repeatable, spawns one creature per line, each a `Spawn`
of one with that line's genes, in the order of the flags and then of the lines,
so the log describes the run as it does for `--gene`. Any `--spawn` or
`--spawn-from` replaces the default ten rabbits, and the two combine. Reading
needs only `species`. Genes a line leaves out come from the baseline, and
`seed`, `day`, `id` and any field it does not know are ignored, so a
hand-written line works and a later field breaks no older reader. A gene out of
bounds or unknown to the species fails before the first day, naming the gene.
The file carries no layout version yet.

`run --help` groups its options under World, Population, Settings and Output.

A comparison script in `tools/` runs variants of a world over a range of seeds,
one `run` per seed and variant in parallel. A variant is a name and a list of
`run` flags, passed through untouched, and the seeds, days, grid and starting
population are shared. It reports, per species, the day each world died out,
its population, and each gene's mean against its mean on day 1, taken on the
last day with enough members to mean something. It never judges: what a result
should show is written as acceptance lines on the roadmap entry that runs it,
and the agent checks the report against them. A named experiment is a preset
in `tools/experiments/`, and flags override it. Presets run seeds 1 to 30,
and `--seeds` overrides that for a quicker or a larger run.

Each variant after the first is compared with the first, on every measure: the
day the species died out, its mean and peak population, and each gene's mean
on the day measured. A world still alive at the end ranks after every world
that died, and two alive worlds tie. A seed too sparse for gene means is left
out of that gene's comparison only. Each comparison gives both variants'
medians, the Vargha-Delaney Â12, which is the chance that a run of the variant
beats a run of the first, and a two-sided Mann-Whitney U p-value. It gives
that p twice: raw, as `p`, and Holm-adjusted across every comparison in the
report, as `pHolm`. The raw p is the one to read for a measure named before
the run, such as a gate's, and the Holm p the one to read when scanning the
report for whatever stands out. The text report says so under its table, and
an acceptance line that sets a p names which one it means. The earlier, later
and tied counts stay beside them.

A second script, `tools/ancestor_competition.py`, runs an ancestor competition
and borrows the comparison script's runner and statistics. It evolves a world
per seed with mutation on and exports its survivors, draws a sample of them
with a seed it records, and then, with mutation off, runs the sample beside the
genome evolution started from, and each group alone, on a test seed of its own.
Mutation off makes every rabbit an exact copy of a founder, so the survivors'
exported genomes say which group each descends from. It reports the evolved
share of the shared world, the population each group sustains alone, and the
evolved genes against their start, and like the comparison it never judges. Its
settings are a preset in `tools/experiments/`, and its files stay under
`target/`, one folder per seed, so a result can be opened afterwards.

## Consequences

Interactive driving costs the agent a server to start and stop. The ready line,
the idle exit and deleting worlds by id remove the usual failures, which are
port clashes, polling for readiness and servers left running.

The `run` command is the headless runner
[0012](0012-tests-target-brains-without-a-world.md) asks for, and it shares
world construction with the server, so a scripted run and a watched one stay
comparable.

The MCP server waits in `docs/ideas/mcp-server.md`, to be built if agents turn
out to drive worlds step by step often enough to justify it.

JSON Lines makes `run` output easy to parse, diff and plot, and nested fields
such as gene means need no new format.

The comparison script knows no species, gene or setting by name. It reads them
from the census, so a new species appears in its report unchanged, and a preset
naming a setting that no longer exists fails in `run` with the setting's name.
Each comparison pays JVM startup once per run, a fraction of each run's time.

An export is a population, not a saved world. It leaves out positions,
reserves, ages, grass and settings, so a world spawned from it starts fresh.
Resuming a world where it stopped is replay from its seed and command log,
which `docs/ideas/world-persistence.md` keeps. The export format is the start of
what `docs/ideas/brain-library.md` saves.
