# 0021. Agents drive worlds over HTTP and through `run`

## Status

Accepted, 2026-10-01. Revised 2026-10-05.

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

## Decision

The jar has three entry points. `serve` is the default and opens the browser.
`serve --no-browser` runs the same HTTP API with no page, for an agent. `run
--seed 42 --days 500` advances one world headless and prints its census as JSON
Lines, one object per day.

`run` takes `--days`, which is required, and `--seed`, which defaults to one
drawn from the clock. `--width` and `--height` default to 20. `--spawn
rabbit=10` gives a starting population, can be repeated once per species, and
defaults to ten rabbits, and any `--spawn` given replaces that default rather
than adding to it. Each line is a census row with the seed added, since batch
runs put many seeds into one stream and a line should stand on its own:

    {"seed":42,"day":1,"population":{"rabbit":10}}

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

`run` gains a batch mode over many seeds in one process when the selection gate
in roadmap entry 7 needs it, and a configuration file overriding declared
settings once there are settings worth overriding.

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
