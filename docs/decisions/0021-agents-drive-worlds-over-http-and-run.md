# 0021. Agents drive worlds over HTTP and through `run`

## Status

Accepted, 2026-10-01.

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

## Decision

The jar has three entry points. `serve` is the default and opens the browser.
`serve --no-browser` runs the same HTTP API with no page, for an agent. `run
--seed 42 --days 500` advances one world headless and prints its census as JSON
Lines, one object per day.

For an agent, `serve --no-browser` accepts `--port 0`, picks a free port, and
prints one JSON line with the port once it accepts requests. It exits on its own
after a configurable idle period. An agent closes a world it is finished with
by deleting it, per [0020](0020-the-interface-is-a-web-page.md), and has no way
to stop the process over HTTP.

Worlds are addressed by id, so one server holds several, such as a control and a
variant side by side.

`run` gains a batch mode over many seeds in one process when the selection gate
in roadmap entry 5 needs it, and a configuration file overriding declared
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
