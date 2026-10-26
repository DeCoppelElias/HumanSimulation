"""Runs variants of a world over a range of seeds and reports how each fared.

It reports and never judges. What a result should show is written on the
roadmap entry that runs it.

    python tools/compare.py tools/experiments/selection-gate.json
    python tools/compare.py --seeds 1-3 --days 200 --variant base= --variant cheap="--set rabbit.speedCharge=0.05"

It needs the jar, from ./mvnw package -Pskip-frontend.
"""

import argparse
import collections
import concurrent.futures
import json
import math
import os
import re
import shlex
import statistics
import subprocess
import sys
from pathlib import Path

# A mean of identical genes can come out a few ulps off them.
ROUNDING = 1e-9
DEFAULT_JAR =Path(__file__).resolve().parent.parent / "target" / "HumanSimulation.jar"


def parse_seeds(text):
    first, _, last = text.partition("-")
    low, high = int(first), int(last or first)
    if high < low:
        raise ValueError(f"seeds run upward, got {text}")
    return list(range(low, high + 1))


def rows_of(lines):
    return [json.loads(line) for line in lines if line.strip()]


def species_in(runs):
    """Every species with gene means on day 1 of any run, so a species with no genes, like grass, is left out."""
    return sorted({species for lines in runs for species in rows_of(lines)[0]["geneMeans"]})


def summarise(lines, species, floor):
    """A run that never had the species dies on day 0, so it reads as absent rather than dying on day 1."""
    rows = rows_of(lines)
    counts = [row["population"].get(species, 0) for row in rows]
    if not any(counts):
        died_on = 0
    else:
        died_on = next((row["day"] for row, count in zip(rows, counts) if count == 0), None)
    alive = [count for count in counts if count > 0]
    measured = [row for row, count in zip(rows, counts) if count >= floor]
    return {
        "diedOn": died_on,
        "meanPopulation": statistics.mean(alive) if alive else 0,
        "peak": max(counts, default=0),
        "start": next((row["geneMeans"][species] for row in rows if species in row["geneMeans"]), {}),
        "measuredOn": measured[-1]["day"] if measured else None,
        "genes": measured[-1]["geneMeans"].get(species, {}) if measured else {},
    }


def _lasted(died_on):
    return float("inf") if died_on is None else died_on


def a12(mine, theirs):
    """The chance that a value from mine beats one from theirs, a tie counting half."""
    wins = sum((x > y) + 0.5 * (x == y) for x in mine for y in theirs)
    return wins / (len(mine) * len(theirs))


def mann_whitney_p(mine, theirs):
    """Two-sided, from the normal approximation with tie and continuity corrections."""
    n1, n2 = len(mine), len(theirs)
    n = n1 + n2
    ties = sum(count**3 - count for count in collections.Counter(mine + theirs).values())
    variance = n1 * n2 / 12 * ((n + 1) - ties / (n * (n - 1)))
    if variance == 0:
        return 1.0
    u = a12(mine, theirs) * n1 * n2
    z = max(abs(u - n1 * n2 / 2) - 0.5, 0) / math.sqrt(variance)
    return min(1.0, math.erfc(z / math.sqrt(2)))


def holm(ps):
    """Holm-adjusted p-values, in the order given."""
    order = sorted(range(len(ps)), key=lambda i: ps[i])
    adjusted = [0.0] * len(ps)
    running = 0.0
    for rank, i in enumerate(order):
        running = max(running, (len(ps) - rank) * ps[i])
        adjusted[i] = min(1.0, running)
    return adjusted


BASE_MEASURES = ("survival", "meanPopulation", "peak")


def measures(summary):
    """Every number a run is compared on, a gene as gene.<name> so it cannot clash with the others."""
    values = {
        "survival": _lasted(summary["diedOn"]),
        "meanPopulation": summary["meanPopulation"],
        "peak": summary["peak"],
    }
    for gene, value in summary["genes"].items():
        values[f"gene.{gene}"] = value
    return values


def _median(values):
    middle = statistics.median(values)
    return "alive" if middle == float("inf") else middle


def compare_variants(results, variants, seeds):
    """A seed with no value for a measure, such as a gene never measured, drops out of that measure only."""
    values = {key: measures(summary) for key, summary in results.items()}
    reference = variants[0]
    comparisons = []
    for variant in variants[1:]:
        genes = sorted(
            {name for v in (variant, reference) for seed in seeds for name in values[(v, seed)] if name.startswith("gene.")}
        )
        for name in [*BASE_MEASURES, *genes]:
            mine = [values[(variant, seed)][name] for seed in seeds if name in values[(variant, seed)]]
            theirs = [values[(reference, seed)][name] for seed in seeds if name in values[(reference, seed)]]
            if not mine or not theirs:
                continue
            comparisons.append(
                {
                    "variant": variant,
                    "reference": reference,
                    "measure": name,
                    "median": _median(mine),
                    "referenceMedian": _median(theirs),
                    "n": len(mine),
                    "referenceN": len(theirs),
                    "a12": a12(mine, theirs),
                    "p": mann_whitney_p(mine, theirs),
                }
            )
    return comparisons


def adjust_across(reports):
    """Holm over every comparison of every species, so pHolm covers the whole report."""
    every = [comparison for one in reports.values() for comparison in one["comparisons"]]
    for comparison, adjusted in zip(every, holm([comparison["p"] for comparison in every])):
        comparison["pHolm"] = adjusted


def report(results, variants, seeds, species, floor=5):
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
                if gene in results[(variant, seed)]["genes"] and gene in results[(variant, seed)]["start"]
            ]
            ended = [value for value, _ in pairs]
            genes[gene][variant] = {
                "min": min(ended, default=None),
                "median": statistics.median(ended) if ended else None,
                "max": max(ended, default=None),
                "above": sum(value - start > ROUNDING for value, start in pairs),
                "of": len(pairs),
            }
    result = {
        "species": species,
        "floor": floor,
        "seeds": seeds,
        "variants": variants,
        "outlasts": outlasts,
        "comparisons": compare_variants(results, variants, seeds),
        "genes": genes,
        "runs": {f"{variant}:{seed}": results[(variant, seed)] for variant in variants for seed in seeds},
    }
    adjust_across({species: result})
    return result


def _number(value):
    return "-" if value is None else f"{value:.2f}"


def format_text(reports, per_seed):
    out = []
    for species, one in reports.items():
        variants, seeds, floor = one["variants"], one["seeds"], one["floor"]
        out.append(f"{species}: survival, the day the last one died")
        out.append("seed  " + "".join(f"{variant:>12}" for variant in variants))
        for seed in seeds:
            cells = []
            for variant in variants:
                died_on = one["runs"][f"{variant}:{seed}"]["diedOn"]
                shown = "alive" if died_on is None else "absent" if died_on == 0 else died_on
                cells.append(f"{shown:>12}")
            out.append(f"{seed:<6}" + "".join(cells))
        for variant in variants:
            alive = sum(one["runs"][f"{variant}:{seed}"]["diedOn"] is None for seed in seeds)
            out.append(f"{variant} alive at the end: {alive} of {len(seeds)}")
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
                    if run["measuredOn"] is None:
                        out.append(f"{variant} {seed} never had {floor} or more")
                        continue
                    genes = " ".join(f"{gene}={value:.2f}" for gene, value in sorted(run["genes"].items()))
                    out.append(f"{variant} {seed} day {run['measuredOn']}: {genes}")
        out.append("")
    return "\n".join(out)


def parse_args(argv):
    parser = argparse.ArgumentParser(description="Run variants of a world over seeds and report how each fared.")
    parser.add_argument("preset", nargs="?", help="a JSON preset, such as tools/experiments/selection-gate.json")
    parser.add_argument("--seeds", help="an inclusive range such as 1-10")
    parser.add_argument("--days", type=int)
    parser.add_argument("--flags", default="", help="run flags shared by every variant, quoted")
    parser.add_argument("--variant", action="append", default=[], help='NAME="run flags", repeatable')
    parser.add_argument("--species", action="append", help="report only these, repeatable")
    parser.add_argument("--floor", type=int, default=5, help="fewest members for gene means to count")
    parser.add_argument("--jobs", type=int, default=os.cpu_count())
    parser.add_argument("--jar", default=str(DEFAULT_JAR))
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--per-seed", action="store_true")
    return parser.parse_args(argv)


def plan(preset, args):
    shared = list(preset.get("flags", [])) + shlex.split(args.flags)
    if args.variant:
        own = {}
        for entry in args.variant:
            name, _, flags = entry.partition("=")
            own[name] = shlex.split(flags)
    else:
        own = preset.get("variants", {})
    if not own:
        raise ValueError("no variants: give a preset or --variant NAME=FLAGS")
    seeds = args.seeds or preset.get("seeds")
    days = args.days or preset.get("days")
    if seeds is None or days is None:
        raise ValueError("seeds and days come from the preset or --seeds and --days")
    return {
        "seeds": parse_seeds(seeds),
        "days": days,
        "variants": {name: shared + list(flags) for name, flags in own.items()},
        "species": args.species or preset.get("species"),
    }


def command(jar, seed, days, flags):
    return ["java", "-jar", str(jar), "run", "--seed", str(seed), "--days", str(days), *flags]


class RunFailed(Exception):
    pass


def check_output(variant, seed, returncode, stdout, stderr):
    """Only the first line of stderr, since picocli follows a usage error with the whole help text."""
    if returncode != 0 or not stdout.strip():
        plain = re.sub(r"\x1b\[[0-9;]*m", "", stderr).strip()
        reason = plain.splitlines()[0] if plain else "run printed nothing"
        raise RunFailed(f"{variant}, seed {seed}: {reason}")
    return stdout.splitlines()


def run_world(jar, variant, seed, days, flags):
    done = subprocess.run(
        command(jar, seed, days, flags), capture_output=True, text=True, encoding="utf-8", errors="replace"
    )
    return check_output(variant, seed, done.returncode, done.stdout, done.stderr)


def main(argv):
    args = parse_args(argv)
    preset = json.loads(Path(args.preset).read_text(encoding="utf-8")) if args.preset else {}
    chosen = plan(preset, args)
    if not Path(args.jar).exists():
        sys.exit(f"no jar at {args.jar}; build it with ./mvnw package -Pskip-frontend")
    variants, seeds = list(chosen["variants"]), chosen["seeds"]
    pool = concurrent.futures.ThreadPoolExecutor(max_workers=args.jobs)
    futures = {
        pool.submit(run_world, args.jar, variant, seed, chosen["days"], chosen["variants"][variant]): (variant, seed)
        for variant in variants
        for seed in seeds
    }
    lines = {}
    try:
        for future in concurrent.futures.as_completed(futures):
            lines[futures[future]] = future.result()
    except RunFailed as failure:
        pool.shutdown(cancel_futures=True)
        sys.exit(str(failure))
    pool.shutdown()
    species = chosen["species"] or species_in(lines.values())
    reports = {}
    for one in species:
        results = {key: summarise(found, one, args.floor) for key, found in lines.items()}
        reports[one] = report(results, variants, seeds, one, args.floor)
    adjust_across(reports)
    print(json.dumps(reports, indent=2) if args.json else format_text(reports, args.per_seed))


if __name__ == "__main__":
    main(sys.argv[1:])
