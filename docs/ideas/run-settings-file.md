# Run settings and genes file

Status: idea.

## What it does

Gives `run` a `--settings FILE` holding many overrides at once, one
`owner.key=value` per line in the syntax of `--set`, with `--set` winning over
the file.

## Why it is interesting

An agent comparing several candidate configurations could keep each as a named
file rather than a long command line. Roadmap entry 7 left it out, since a shell
repeats a long command for free and tuned values become the defaults in code.

## What it would touch

The `run` command only. Nothing in the domain.

Elias's trigger for building it: once a preset's flags grow much past the
grass ramp and the handful of `--gene` overrides that entry 7's gate carries,
a file of settings, or of starting genes, beats a longer list of flags.

## Open questions

Whether genes and settings share one file or a gene preset is its own.

Whether the format is `--set` lines or the snapshot's JSON grouped by owner,
which would let a running world's values be saved and fed back.
