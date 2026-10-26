"""Runs variants of a world over a range of seeds and reports how each fared.

It reports and never judges. What a result should show is written on the
roadmap entry that runs it.

    python tools/compare.py tools/experiments/mutation-ramp.json
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
DEFAULT_JAR = Path(__file__).resolve().parent.parent / "target" / "HumanSimulation.jar"


def parse_seeds(text):
    first, _, last = text.partition("-")
    low, high = int(first), int(last or first)
    if high < low:
        raise ValueError(f"seeds run upward, got {text}")
    return list(range(low, high + 1))


def rows_of(lines):
    return [json.loads(line) for line in lines if line.strip()]


def species_in(runs):
    """Every species with gene means on day 1 of any run, or a death on any day, so grass appears through its deaths."""
    found = set()
    for lines in runs:
        rows = rows_of(lines)
        found.update(rows[0]["geneMeans"])
        for row in rows:
            found.update(row.get("deaths", {}))
    return sorted(found)


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
    totals = collections.Counter()
    for row in rows:
        totals.update(row.get("deaths", {}).get(species, {}))
    died = sum(totals.values())
    return {
        "diedOn": died_on,
        "meanPopulation": statistics.mean(alive) if alive else 0,
        "peak": max(counts, default=0),
        "start": next((row["geneMeans"][species] for row in rows if species in row["geneMeans"]), {}),
        "measuredOn": measured[-1]["day"] if measured else None,
        "genes": measured[-1]["geneMeans"].get(species, {}) if measured else {},
        "deaths": {cause: count / died for cause, count in sorted(totals.items())} if died else {},
    }


def fill_causes(results):
    """A cause one run never saw is a share of 0 in every run that had deaths. One cause alone says nothing, so it goes."""
    causes = sorted({cause for summary in results.values() for cause in summary["deaths"]})
    for summary in results.values():
        if len(causes) < 2:
            summary["deaths"] = {}
        elif summary["deaths"]:
            summary["deaths"] = {cause: summary["deaths"].get(cause, 0.0) for cause in causes}


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


def measures(summary, has_genes):
    """Every number a run is compared on, a gene as gene.<name> and a cause as deaths.<name> so none can clash.

    A species without genes is compared on its deaths alone. Means and shares are rounded to 9 decimals, as ROUNDING
    allows, so two that differ by a few ulps tie rather than rank.
    """
    values = {}
    if has_genes:
        values["survival"] = _lasted(summary["diedOn"])
        values["meanPopulation"] = summary["meanPopulation"]
        values["peak"] = summary["peak"]
        for gene, value in summary["genes"].items():
            values[f"gene.{gene}"] = round(value, 9)
    for cause, share in summary["deaths"].items():
        values[f"deaths.{cause}"] = round(share, 9)
    return values


def _median(values):
    """Alive when at least half the worlds were still alive at the end."""
    middle = statistics.median(values)
    return "alive" if middle == float("inf") else middle


def compare_variants(results, variants, seeds, has_genes):
    """A seed with no value for a measure, such as a gene never measured, drops out of that measure only."""
    values = {key: measures(summary, has_genes) for key, summary in results.items()}
    reference = variants[0]
    comparisons = []
    for variant in variants[1:]:
        named = {name for v in (variant, reference) for seed in seeds for name in values[(v, seed)]}
        genes = sorted(name for name in named if name.startswith("gene."))
        deaths = sorted(name for name in named if name.startswith("deaths."))
        for name in [*(BASE_MEASURES if has_genes else ()), *genes, *deaths]:
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
    deaths = {}
    for cause in sorted({cause for summary in results.values() for cause in summary["deaths"]}):
        deaths[cause] = {}
        for variant in variants:
            shares = [
                results[(variant, seed)]["deaths"][cause] for seed in seeds if results[(variant, seed)]["deaths"]
            ]
            deaths[cause][variant] = {
                "min": min(shares, default=None),
                "median": statistics.median(shares) if shares else None,
                "max": max(shares, default=None),
                "of": len(shares),
            }
    has_genes = any(summary["start"] for summary in results.values())
    result = {
        "species": species,
        "floor": floor,
        "seeds": seeds,
        "variants": variants,
        "hasGenes": has_genes,
        "outlasts": outlasts,
        "comparisons": compare_variants(results, variants, seeds, has_genes),
        "genes": genes,
        "deaths": deaths,
        "runs": {f"{variant}:{seed}": results[(variant, seed)] for variant in variants for seed in seeds},
    }
    adjust_across({species: result})
    return result


def report_all(lines, variants, seeds, species, floor):
    """One report per species, every species in the census when none are named, with Holm over all of them."""
    reports = {}
    for one in species or species_in(lines.values()):
        results = {key: summarise(found, one, floor) for key, found in lines.items()}
        fill_causes(results)
        reports[one] = report(results, variants, seeds, one, floor)
    adjust_across(reports)
    return reports


def _number(value):
    return "-" if value is None else f"{value:.2f}"


LEGEND = "raw p is for a measure named before the run, such as a gate's; Holm p is for scanning the whole report"


def _median_text(value):
    return value if isinstance(value, str) else f"{value:.2f}"


def _lives(species, one):
    """Survival and gene means, which only a species with genes has."""
    variants, seeds = one["variants"], one["seeds"]
    out = [f"{species}: survival, the day the last one died"]
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
    return out


def _deaths(species, one):
    out = [f"{species}: deaths by cause, share min / median / max"]
    for cause, row in one["deaths"].items():
        cells = [f"{cause:<12}"]
        for variant in one["variants"]:
            d = row[variant]
            cells.append(f"  {variant} {_number(d['min'])} / {_number(d['median'])} / {_number(d['max'])}")
        out.append("".join(cells))
    out.append("")
    return out


def _comparisons(species, one):
    variants = one["variants"]
    out = [f"{species}: each variant against {variants[0]}, medians, A12, raw p, Holm p"]
    width = max(len(variant) for variant in variants[1:])
    for c in one["comparisons"]:
        out.append(
            f"{c['measure']:<16} {c['variant']:<{width}} {_median_text(c['median'])}"
            f" vs {_median_text(c['referenceMedian'])}"
            f"  A12 {c['a12']:.2f}  p {c['p']:.3f}  Holm p {c['pHolm']:.3f}"
        )
    out.append(LEGEND)
    out.append("")
    return out


def _per_seed(species, one):
    out = [f"{species}: population, mean over days alive / peak"]
    out.append("seed  " + "".join(f"{variant:>16}" for variant in one["variants"]))
    for seed in one["seeds"]:
        cells = []
        for variant in one["variants"]:
            run = one["runs"][f"{variant}:{seed}"]
            cells.append(f"{run['meanPopulation']:>9.1f} / {run['peak']:<4}")
        out.append(f"{seed:<6}" + "".join(cells))
    out.append("")
    out.append(f"{species}: gene means per seed, on the day measured")
    for variant in one["variants"]:
        for seed in one["seeds"]:
            run = one["runs"][f"{variant}:{seed}"]
            if run["measuredOn"] is None:
                out.append(f"{variant} {seed} never had {one['floor']} or more")
                continue
            genes = " ".join(f"{gene}={value:.2f}" for gene, value in sorted(run["genes"].items()))
            out.append(f"{variant} {seed} day {run['measuredOn']}: {genes}")
    out.append("")
    return out


def format_text(reports, per_seed):
    """A species without genes, like grass, shows only its deaths."""
    out = []
    for species, one in reports.items():
        if one["hasGenes"]:
            out.extend(_lives(species, one))
        if one["deaths"]:
            out.extend(_deaths(species, one))
        if one["comparisons"]:
            out.extend(_comparisons(species, one))
        if per_seed and one["hasGenes"]:
            out.extend(_per_seed(species, one))
    return "\n".join(out)


def parse_args(argv):
    parser = argparse.ArgumentParser(description="Run variants of a world over seeds and report how each fared.")
    parser.add_argument("preset", nargs="?", help="a JSON preset, such as tools/experiments/mutation-ramp.json")
    parser.add_argument("--seeds", help="an inclusive range such as 1-30")
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
    reports = report_all(lines, variants, seeds, chosen["species"], args.floor)
    print(json.dumps(reports, indent=2) if args.json else format_text(reports, args.per_seed))


if __name__ == "__main__":
    main(sys.argv[1:])
