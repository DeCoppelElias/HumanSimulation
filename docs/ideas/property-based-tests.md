# Property-based tests

Status: idea.

## What it does

Tests the domain's invariants with jqwik, which generates inputs, checks a
property against each, and shrinks a failure to the smallest input that still
breaks it.

## Why it is interesting

Several rules are statements about every input rather than one example: a move
never walks off the step distribution, a mutated step distribution stays a
valid simplex, a view range never drops below one, the census covers every
recorded day. A seeded loop in plain JUnit checks them over thousands of draws
and replays a failure exactly, but reports it as a seed and an iteration. A
shrunk counterexample, such as a two-entry simplex with one weight at zero,
says what is wrong without a debugger.

## What it would touch

`pom.xml`, as a test-scoped engine beside Jupiter, and the tests of `Genome`,
`GeneSpec` mutation and the resolver. No production code.

## Open questions

Whether jqwik runs on the JUnit 6 platform, or would force a JUnit downgrade.

Whether entry 6, when mutation produces arbitrary genomes, is the point where
shrinking starts to pay, or whether the seeded loops stay enough.

How generated inputs draw their randomness, so that a property test does not
create a second source of random numbers beside the world's one generator.
