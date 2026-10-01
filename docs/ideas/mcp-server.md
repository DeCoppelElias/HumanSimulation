# MCP server

Status: idea.

## What it does

Exposes a world to an AI agent as a Model Context Protocol server over stdio,
with a handful of typed tools such as `new_world`, `submit`, `advance`,
`snapshot` and `run_batch`. The agent's client starts and stops the process,
so the world stays alive between calls without the agent managing a server.

## Why it is interesting

An agent driving a world step by step over the local HTTP API has to start the
server, wait for it, remember the port and stop it afterwards. Stale servers
and port clashes are the usual failures. Over stdio the client owns that
lifecycle. A study by Scale Labs comparing the same backend behind MCP and a
CLI found similar success rates for current models, with far fewer turns per
task over MCP.

## What it would touch

A new adapter beside the web and command-line ones, sending commands in and
reading snapshots and census rows out, per
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md). It adds
the official MCP Java SDK as a dependency, kept out of `domain` by the `jdeps`
test.

## Open questions

Whether interactive driving is something an agent needs often enough to justify
a third adapter, or whether batch runs cover most of it.

How large a result can be before it floods the agent's context. A 500-day trace
is better as a file the tool writes than as a tool result.

Whether the agent should be able to reach both the HTTP API and the MCP server.
The same study found that offering both and letting the model choose did not
help.
