# 0020. The interface is a web page over a Java core

## Status

Accepted, 2026-10-01.

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

Java was measured against the agent goal and passes. A day at the target size
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
and one stream. Javalin is a thin layer over Jetty that `Main` constructs by
hand.

## Decision

The core stays Java and depends only on `java.base`. Everything lives under
`io.github.eliasdecoppel.humansimulation`, with the domain in `.domain`.

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
it. A viewer that falls behind gets the latest snapshot rather than a backlog.
Census rows are never dropped, since the chart needs every day, and the adapter
keeps their history so a reloaded page restores the chart.

Worlds are addressed by an id of 128 random bits from `SecureRandom`, drawn
apart from the world's seeded generator so replay is unaffected, and the number
of worlds is capped. A world is closed by deleting it. There is no endpoint that
stops the process.

`java -jar` runs the server locally and opens the browser. Hosting it is roadmap
entry 8, which adds sessions, ownership, clean-up of abandoned worlds and a
deploy to Fly.io.

## Consequences

The app can become a link without leaving Java, at a small hosting cost and a
cold start after idle periods.

The project has two languages and two toolchains. The Maven build hides the
second from anyone who only runs `./mvnw`.

The smoke test that drove Swing with `java.awt.Robot` has nothing to drive.
Frontend logic is tested with Vitest and the API with JUnit against a running
Javalin. Checking what renders is a person watching the page until a
browser-driven test earns its place, which `docs/ideas/browser-smoke-test.md`
holds.

JFreeChart, the Swing panels and the icons at the classpath root go with the
2022 code.

If traffic ever makes server costs matter, the `java.base`-only core keeps
compiling it to the browser with TeaVM available.

Smooth movement needs no change to the domain, because snapshots already carry
ids. A hop animation or a death effect wants events, which are an additive field
on the report rather than a new channel.
