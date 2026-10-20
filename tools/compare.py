"""Runs variants of a world over a range of seeds and reports how each fared.

It reports and never judges. What a result should show is written on the
roadmap entry that runs it.
"""

import json
import statistics


def parse_seeds(text):
    first, _, last = text.partition("-")
    low, high = int(first), int(last or first)
    if high < low:
        raise ValueError(f"seeds run upward, got {text}")
    return list(range(low, high + 1))


def rows_of(lines):
    return [json.loads(line) for line in lines if line.strip()]


def species_in(lines):
    """Every species with gene means on day 1, so a species with no genes, like grass, is left out."""
    return sorted(rows_of(lines)[0]["geneMeans"])


def summarise(lines, species, floor):
    rows = rows_of(lines)
    counts = [row["population"].get(species, 0) for row in rows]
    died_on = next((row["day"] for row, count in zip(rows, counts) if count == 0), None)
    alive = [count for count in counts if count > 0]
    measured = [row for row, count in zip(rows, counts) if count >= floor]
    return {
        "diedOn": died_on,
        "meanPopulation": statistics.mean(alive) if alive else 0,
        "peak": max(counts, default=0),
        "start": rows[0]["geneMeans"].get(species, {}),
        "measuredOn": measured[-1]["day"] if measured else None,
        "genes": measured[-1]["geneMeans"].get(species, {}) if measured else {},
    }


def _lasted(died_on):
    return float("inf") if died_on is None else died_on


def report(results, variants, seeds, species):
    """Each variant after the first is counted against the first, seed by seed."""
    reference = variants[0]
    outlasts = []
    for variant in variants[1:]:
        later = ties = earlier = 0
        for seed in seeds:
            mine = _lasted(results[(variant, seed)]["diedOn"])
            theirs = _lasted(results[(reference, seed)]["diedOn"])
            later += mine > theirs
            ties += mine == theirs
            earlier += mine < theirs
        outlasts.append(
            {"variant": variant, "reference": reference, "later": later, "ties": ties, "earlier": earlier}
        )
    genes = {}
    for variant in variants:
        for seed in seeds:
            for gene in results[(variant, seed)]["start"]:
                genes.setdefault(gene, {})
    for gene in genes:
        starts = [results[(reference, seed)]["start"].get(gene) for seed in seeds]
        starts = [value for value in starts if value is not None]
        genes[gene]["start"] = statistics.median(starts) if starts else None
        for variant in variants:
            pairs = [
                (results[(variant, seed)]["genes"][gene], results[(variant, seed)]["start"][gene])
                for seed in seeds
                if gene in results[(variant, seed)]["genes"]
            ]
            ended = [value for value, _ in pairs]
            genes[gene][variant] = {
                "min": min(ended, default=None),
                "median": statistics.median(ended) if ended else None,
                "max": max(ended, default=None),
                "above": sum(value > start for value, start in pairs),
                "of": len(pairs),
            }
    return {
        "species": species,
        "seeds": seeds,
        "variants": variants,
        "outlasts": outlasts,
        "genes": genes,
        "runs": {f"{variant}:{seed}": results[(variant, seed)] for variant in variants for seed in seeds},
    }


def _number(value):
    return "-" if value is None else f"{value:.2f}"


def format_text(reports, per_seed):
    out = []
    for species, one in reports.items():
        variants, seeds = one["variants"], one["seeds"]
        out.append(f"{species}: survival, the day the last one died")
        out.append("seed  " + "".join(f"{variant:>12}" for variant in variants))
        for seed in seeds:
            cells = []
            for variant in variants:
                died_on = one["runs"][f"{variant}:{seed}"]["diedOn"]
                cells.append(f"{'alive' if died_on is None else died_on:>12}")
            out.append(f"{seed:<6}" + "".join(cells))
        for pair in one["outlasts"]:
            out.append(
                f"{pair['variant']} later than {pair['reference']}: {pair['later']} of {len(seeds)},"
                f" ties {pair['ties']}, earlier {pair['earlier']}"
            )
        out.append("")
        out.append(f"{species}: gene means against day 1, min / median / max, seeds above their start")
        for gene, row in sorted(one["genes"].items()):
            cells = [f"{gene:<12} start {_number(row['start'])}"]
            for variant in variants:
                g = row[variant]
                cells.append(
                    f"  {variant} {_number(g['min'])} / {_number(g['median'])} / {_number(g['max'])}"
                    f" above {g['above']} of {g['of']}"
                )
            out.append("".join(cells))
        out.append("")
        out.append(f"{species}: population, mean over days alive / peak")
        out.append("seed  " + "".join(f"{variant:>16}" for variant in variants))
        for seed in seeds:
            cells = []
            for variant in variants:
                run = one["runs"][f"{variant}:{seed}"]
                cells.append(f"{run['meanPopulation']:>9.1f} / {run['peak']:<4}")
            out.append(f"{seed:<6}" + "".join(cells))
        if per_seed:
            out.append("")
            out.append(f"{species}: gene means per seed, on the day measured")
            for variant in variants:
                for seed in seeds:
                    run = one["runs"][f"{variant}:{seed}"]
                    genes = " ".join(f"{gene}={value:.2f}" for gene, value in sorted(run["genes"].items()))
                    out.append(f"{variant} {seed} day {run['measuredOn']}: {genes}")
        out.append("")
    return "\n".join(out)
