# Brain library

Status: idea.

## What it does

Saves the genome of a creature whose behaviour you like, and loads it later as
a species' baseline or as the genome of a spawned creature. A saved brain is a
genome plus the name of its species and a version of its gene layout.

## Why it is interesting

Under [0006](../decisions/0006-genomes-are-a-named-layout-of-gene-shapes.md)
and [0018](../decisions/0018-brain-computation-model-and-warm-starting.md) a
brain's parameters are genes, so a good brain found in one run is otherwise
lost when the run ends. Keeping it lets a later run start from evolved
behaviour, and lets one population be dropped into another world.

It pairs with the warm start in 0018. A network brain fitted once to imitate
the fixed-rules brain is the first entry such a library would hold.

## What it would touch

Genomes, species and their layouts need a stable serialised form, which is
cheap while they are plain records. Loading needs a command, per
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md). A
saved genome whose layout version no longer matches its species needs a rule.

## Open questions

Whether a layout change invalidates saved genomes or migrates them.

Where the library lives: files beside the jar, the browser, or the server once
the app is hosted.

Whether fitting the seed network counts as the training `DESIGN.md` rules out.
The intent is that it does not, since the brain is fitted once and then left to
selection.
