# 0022. Warnings fail the build, and nothing is null

## Status

Accepted, 2026-10-04.

## Context

Roadmap entry 1 deletes every line of 2022 code, so the rules new code is
written to could be chosen without regard for what came before. The decisions
up to here settle architecture and testing and leave the code-level standards
open: the Java version, what the compiler checks, how absence and bad input are
handled, how the command line is parsed, and how formatting is checked.

The project targeted Java 21. Java 25 has been the current long-term release
since September 2025, and nothing ties the rebuild to 21. What it adds that this
code uses is unnamed variables in pattern switches over sealed types and
validation before `this(...)` in constructors.

Error Prone was considered beside the compiler's own warnings. It catches bug
patterns javac does not, at the cost of `--add-exports` flags in
`.mvn/jvm.config` and releases that trail a new JDK by a month or two. NullAway,
which runs on Error Prone, would check nulls at compile time and needs every
interface annotated. A small codebase that is test-first and reviewed per entry
gets less from either than a large one would. Error Prone is kept in
`docs/ideas/error-prone.md`.

`Objects.requireNonNull` on every parameter was the usual alternative to a
policy of never passing null. When nothing passes null, a null is already a bug
that fails fast, and the checks are noise.

A hand-written argument parser was the alternative to picocli. It is thirty
lines for `run --seed --days`, and by the time `run` takes setting overrides and
seed ranges and `serve` takes its own flags, it is a homemade picocli with worse
error messages. Agents are the main users of `run`, and a precise message for a
wrong flag is worth more to them than to a person.

Spotless checked only files that differ from `origin/master`, so formatting the
2022 code would not move `git blame` off it. Entry 1 deletes that code, which
leaves the ratchet nothing to protect. And on a push to `master` it compares the
branch with itself, so the check in CI does nothing there.

Javadoc on every public domain type, with decision numbers cited in comments,
was considered. The comment rules and good names are judged enough for now, and
it is kept in `docs/ideas/javadoc-on-the-domain.md`.

## Decision

Java 25.

The compiler runs with `-Xlint:all -Werror`, with only the `serial` lint off.
Every other warning fails the build.

Nothing is null. Absence is an `Optional` or an empty collection, no method
takes or returns `null`, and no field holds it. Parameters are not checked for
null.

Values validate themselves in their compact constructors and throw
`IllegalArgumentException` naming what is wrong, so an invalid `Genome`, a
simplex that does not sum to one, or a `Move` of distance zero cannot exist. A
list held by a record is copied with `List.copyOf`, which makes it immutable and
rejects null elements. A map is copied into an unmodifiable map sorted by key,
never with `Map.copyOf`, whose iteration order changes from one JVM start to the
next and would make the same seed print different lines, against
[0010](0010-runs-replay-exactly-from-a-seed.md). A command is validated before
it changes the world, per
[0007](0007-state-leaves-as-a-snapshot-commands-go-in.md).

The command line is parsed with picocli, in the `.cli` adapter only, so the
`jdeps` rule from 0007 is untouched. `run` is a picocli command, and `serve`
joins it as a sibling.

Spotless formats every Java file with palantir-java-format and checks it in
`./mvnw verify`, with no ratchet.

## Consequences

Running the jar needs Java 25, which the README states. If palantir-java-format
cannot parse Java 25 syntax when entry 1 starts, the project stays on 21 until
it can, and this decision is revised to say so.

A warning cannot be left for later, which keeps the count at zero while that is
cheap and makes upgrading the JDK the moment new warnings are dealt with.

A `null` that does reach the domain is a bug found by an exception rather than
by a check, so the stack trace points where it was used rather than where it
came from.

A value that exists is valid, so no system checks its inputs again, and a test
of a rule never has to consider a malformed genome.

picocli is a dependency, shaded into the jar and proposed for upgrade by
Dependabot like the others.

Formatting the whole tree is safe from entry 1 on, since every file in it is
new.
