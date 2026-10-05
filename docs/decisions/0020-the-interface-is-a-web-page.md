# 0020. The interface is a web page over a Java core

## Status

Accepted, 2026-10-01. Revised 2026-10-05.

## Context

`DESIGN.md` sets two goals: the world is pretty and satisfying enough that
people open it to watch, and an AI agent can experiment with it easily. The
2022 interface is Swing, which looks dated, runs only where Java is installed,
and gives an agent nothing to drive.

The rebuild takes no code from 2022, per
[0002](0002-replace-the-model-layer-in-place.md), and no domain code existed
when this was decided, so the language and the toolkit were both open at the
lowest cost they will ever have.

Swing was the first alternative. It ships with the JDK, JFreeChart and the
robot-driven smoke test already use it, and it looks old. Swing with FlatLaf
fixes the look for one dependency and keeps every tool working, but it stays a
desktop window someone has to install Java to open.

JavaFX has styling, a scene graph and animation. It is no longer part of the
JDK, so the jar becomes platform-specific, CI on Linux and Windows needs care,
and the chart and the smoke test need rework.

TypeScript for everything was the strongest alternative. The core would run in
the browser, so the app is a static link with no server and no install, and the
same core would run in Node for agents. It loses the Java foundation that
already exists, and Elias works in Java. A Java core compiled to the browser
with TeaVM keeps Java and the static link, at the price of a niche toolchain,
partial `java.base` coverage and painful debugging. A Rust core is the fastest
option and a steep first language for a project that needs no speed at a few
hundred tiles.

Java was estimated against the agent goal and passes. A day at the target size
is well under a millisecond, a 500-day run a fraction of a second, and seeds
run in parallel one world per thread. JVM startup is the only cost an agent
notices, and batch runs pay it once.

Hosting decides whether a Java core can still be a link. A Java server on a
host gives every visitor their own world over the network, which costs a small
running fee and a cold start, where a static site costs nothing. That trade is
acceptable at hobby traffic.

Spring Boot was considered for the server and is familiar. It brings a
container, which [0007](0007-state-leaves-as-a-snapshot-commands-go-in.md)
rejects, a slower start and more memory, for a server that needs a few routes
and one stream. Javalin, at version 7 on Jetty 12 when this was decided, is a
thin layer that `Main` constructs by hand.

A queue of census rows per viewer was the first shape of the stream. The
world's thread would add each day's row to every viewer's queue, a writer per
viewer would drain it, and a viewer too far behind would be disconnected. It
guarantees delivery from the server, at the cost of a queue size to choose, a
rule for closing slow viewers, and joining that has to copy the history and
subscribe in one step. The history the adapter keeps already lets a reader
repair a gap, which makes the guarantee unnecessary.

A server that listens on every interface, Javalin's default, lets anyone on the
same network drive its worlds, and the API has no notion of who is calling
until hosting adds one.

## Decision

The core stays Java and depends only on `java.base`. Everything lives under
`io.github.decoppelelias.humansimulation`, with the domain in `.domain`.

The interface is a web page. A Javalin adapter serves it, takes commands as
JSON over HTTP, and streams each world's snapshots and census rows over
Server-Sent Events. Jackson turns records into JSON and back, outside the
domain.

The frontend is React with TypeScript, built by Vite. The grid is a PixiJS
canvas inside one React component, drawn imperatively, and React renders the
controls, the chart and the generated settings panel around it.
`frontend-maven-plugin` downloads Node and builds the frontend into the jar's
resources, so `./mvnw verify` builds and tests both halves.

The browser animates between snapshots. It diffs consecutive snapshots by
entity id, glides a moved entity between its two tiles, fades in an id that
appears and fades out one that is gone. Effects that need more than a diff can
see arrive as events on the day's report.

Each world has one executor thread that owns it. A timer on that thread
advances the world at the playback rate, and request handlers only hand work to
it, so the thread never waits on the network. Each viewer holds only the
latest day's event, its snapshot with that day's census row, overwritten when a
newer one arrives, so a slow viewer skips days instead of slowing the world.
The adapter keeps every census row, and a viewer that sees a gap in day numbers
fetches the missing rows from that history, so the chart never loses a day and
a reloaded page restores it. A comment line every 15 seconds keeps a quiet
stream open.

Worlds are addressed by an id of 128 random bits from `SecureRandom`, drawn
apart from the world's seeded generator so replay is unaffected, and the number
of worlds is capped. The server lists the worlds it holds and keeps each one
until it is deleted, so a person can always go back to a world while the server
runs. There is no endpoint that stops the process. The server listens on
`127.0.0.1` unless told otherwise.

`java -jar` runs the server locally and opens the browser. Hosting it is roadmap
entry 11, which adds sessions, ownership, clean-up of abandoned worlds and a
deploy to Fly.io.

## Consequences

The app can become a link without leaving Java, at a small hosting cost and a
cold start after idle periods.

The project has two languages and two toolchains. The Maven build hides the
second from anyone who only runs `./mvnw`.

The smoke test that drove Swing with `java.awt.Robot` has nothing to drive.
Frontend logic is tested with Vitest and the API with JUnit against a running
Javalin. Checking what renders is a skill an agent runs on demand in a real
browser, from roadmap entry 4, with Elias judging how it looks.

JFreeChart, the Swing panels and the icons at the classpath root go with the
2022 code.

If traffic ever makes server costs matter, the `java.base`-only core keeps
compiling it to the browser with TeaVM available.

Smooth movement needs no change to the domain, because snapshots already carry
ids. A hop animation or a death effect wants events, which are an additive field
on the report rather than a new channel.
