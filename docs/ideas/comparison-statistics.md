# Comparison statistics

Status: idea.

## What it does

Adds an effect size and a significance test to `tools/compare.py`'s report,
beside the counts it gives today. For each pair of variants and each measure,
such as the day a species died out or its mean population, the report gives
the Vargha-Delaney Â12, the chance that one run of the first variant beats one
run of the second, and a two-sided Mann-Whitney U p-value. Presets move to 30
or more seeds.

## Why it is interesting

Counts over ten seeds cannot tell a real effect from luck. Entry 7's gate asks
for 8 of 10, and a variant that truly wins 70% of the time passes that only
about 38% of the time. Tuning runs on the same ten seeds also fit settings to
those seeds. Â12 and Mann-Whitney are the usual pair for randomised algorithms
(Arcuri and Briand, ICSE 2011), and both use every seed's value rather than a
win or a loss, so a tie no longer counts as a failure.

Every other way of testing evolution in these ideas reports through it.

## What it would touch

`tools/compare.py` and its tests. Both statistics are short enough to write by
hand, so the script stays free of dependencies. Presets in `tools/experiments/`
change their seed ranges. The script still never judges: thresholds stay in the
roadmap entry that sets them.

## Open questions

How many seeds a preset needs, given how long a 1000-day run takes.

Whether to correct for several comparisons when a preset has more than two
variants.

Whether a gate's threshold is written as an Â12, a p-value or both.
