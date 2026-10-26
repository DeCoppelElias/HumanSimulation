# Comparison statistics

Status: built, [0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).

## What it does

Adds an effect size and a significance test to `tools/compare.py`'s report,
beside the counts it gives today. For each variant against the first and each
measure, such as the day a species died out or its mean population, the report
gives both medians, the Vargha-Delaney Â12, the chance that one run of the
variant beats one run of the first, and a two-sided Mann-Whitney U p-value,
raw and Holm-adjusted across the report. Presets move to 30 seeds.

## Why it is interesting

Counts over ten seeds cannot tell a real effect from luck. Entry 7's first
selection gate asked for 8 of 10, and a variant that truly wins 70% of the
time passes that only about 38% of the time. Tuning runs on the same ten seeds also fit settings to
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

Whether a gate's threshold is written as an Â12, a p-value or both, settled in
entry 9's grill.
