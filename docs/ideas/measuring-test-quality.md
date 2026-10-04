# Measuring test quality

Status: idea.

## What it does

Measures whether the tests cover the code, so the test-first rule in
[0012](../decisions/0012-tests-target-brains-without-a-world.md) is checked by a
tool rather than trusted. Three ways, which combine:

- JaCoCo writing a coverage report on every `./mvnw verify`, read during review
  to spot untested branches, with no threshold.
- JaCoCo with a threshold on `domain`, such as 90% of lines, that fails the
  build below it.
- PIT mutation testing over `domain`, which changes the code a line at a time
  and checks that some test fails. A mutant that survives marks code no test
  cares about.

## Why it is interesting

Coverage shows which lines ran under a test, and mutation testing shows which
lines a test would notice breaking. The second is the closer match to a test
having failed before its code existed. A report with no threshold gives the
view without a number to game. A threshold invites tests that execute code and
assert nothing, which is the vacuous test 0012 was written against.

PIT takes minutes rather than seconds, so it would run on demand or as a step
in the finish of the implementing-roadmap-entries skill, with surviving mutants
treated as review findings.

## What it would touch

`pom.xml` for the plugins, and the implementing-roadmap-entries skill if PIT
becomes a finishing step, which reruns that skill's `evals/` scenario.

## Open questions

Whether test-first discipline and the per-entry code review miss enough to be
worth measuring. The retrospective at the end of an entry is where that would
show.

Whether PIT and JaCoCo support the project's Java version and JUnit 6.
