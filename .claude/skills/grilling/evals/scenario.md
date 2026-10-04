# Scenario: grilling before a roadmap entry

Run on a fresh agent in this repository, with the skill available, as a
planning exercise: it must not edit files, branch or build. It has no question
tool, so it writes out each question it would ask, with options, and stops
after the third.

## Prompt

> Elias writes: "Before we plan roadmap entry 5, grill me on whatever it leaves
> open."

## Pass

- It reads entry 5, its decisions and the 2022 food code before asking.
- It opens with the gaps it found, such as the fixed-rules brain's rule set
  needing a log entry first, or the crowd rule still undecided.
- It asks one topic per question, each with context and a recommendation, and
  never asks something the docs or code already answer.
- It writes nothing to the docs.

## Baseline without the skill, 2026-10-01

Entry 5 was numbered 2 when these ran.

Read the entry, its decisions, the ideas and the 2022 food code, and found real
drift. It opened with a short list of what was open, then asked three questions,
each with context and a recommendation. It did not say what an answer settled
before the next question, and its questions assumed a reader who had seen its
earlier message.

## With the skill, 2026-10-01

Passed. It read the 2022 code entry 5 replaces as well as the docs, opened with
eight gaps, ordered its questions so the first answer fed the second, and wrote
each question to stand alone inside a question box, opening with what the last
answer settled. It wrote nothing.

## With the skill, 2026-10-04, after the step for reading deleted code changed

Passed on every point. It read entry 5, its decisions and the 2022 food code,
opened with eight gaps, and asked three single-topic questions, each saying
what the last one settled, with context and a recommendation. It checked the
new instruction for reading deleted 2022 code by running the same command
against the flat layout's `src/Main.java`, which found the deleting commit and
read the file back.
