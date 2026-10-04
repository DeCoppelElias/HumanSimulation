# Error Prone

Status: idea.

## What it does

Adds Error Prone to the compiler, so the build fails on bug patterns javac does
not warn about: an ignored return value from an immutable method, `equals` on
arrays, a format string that does not match its arguments, a switch that falls
through.

## Why it is interesting

The build already fails on every javac warning. Error Prone catches a further
class of mistakes that compile cleanly and that a reviewer reads past, and it
does so on every build rather than once per entry. NullAway runs as an Error
Prone plugin, so this is also the way to compile-time null checking if the null
policy ever wants it.

## What it would touch

`pom.xml`, where it joins the compiler plugin as an annotation processor path,
and `.mvn/jvm.config`, which needs the `--add-exports` flags that let it reach
javac's internals. No source changes, beyond fixing what it finds.

## Open questions

Whether its release for the Java version the project is on is out yet. It
trails new JDKs by a month or two, and a build that cannot move to a new JDK
until a plugin catches up is the cost.

Which of its warnings to raise to errors, and whether any should be turned off.
