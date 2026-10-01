# Run comparison

Status: idea.

## What it does

Turns two or more runs into a verdict an agent can read: which variant kept more
creatures alive, how far each gene drifted, and whether the difference holds
across seeds. Input is the JSON Lines that `run` prints, output is a short
summary per variant.

## Why it is interesting

An agent testing a new feature or brain has to answer "did this help?", and
today it would write that comparison from scratch every time, a little
differently each time. One shared comparison makes results from different
sessions comparable, and it is the measuring stick the selection gate in
roadmap entry 7 needs anyway.

## What it would touch

A mode of the `run` command from
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md), or a small
script in `tools/` reading its output. Nothing in the domain.

## Open questions

Whether it lives in Java beside `run` or as a Python script, which is quicker
to change and needs a second runtime.

Which statistics count as a difference worth acting on, which entry 7's gate
has to settle first.
