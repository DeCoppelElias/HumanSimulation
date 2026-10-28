"""Evolves populations, then sets the evolved genomes against the genome they started from.

It reports and never judges. What a result should show is written on the
roadmap entry that runs it.

    python tools/ancestor_competition.py tools/experiments/rabbit-learns-gate.json
    python tools/ancestor_competition.py tools/experiments/rabbit-learns-gate.json --seeds 1-3

It needs the jar, from ./mvnw package -Pskip-frontend.
"""

import argparse
import concurrent.futures
import json
import os
import random
import shutil
import statistics
import sys
import time
from pathlib import Path

import compare

WORK = Path(__file__).resolve().parent.parent / "target" / "ancestor-competition"


def evolve_flags(preset, export):
    """Founders spawn with mutation off, so they are identical, and mutation starts once day 1 is complete."""
    evolve = preset["evolve"]
    flags = [
        *preset["world"],
        "--spawn", f"{preset['species']}={evolve['founders']}",
        *evolve["flags"],
        "--set", "world.mutation=0",
        "--set-at", "1:world.mutation=1",
        "--export", str(export),
    ]
    for day in sorted({1, *evolve["exportDays"], evolve["days"]}):
        flags += ["--export-day", str(day)]
    return flags


def test_flags(preset, sources, export):
    flags = [*preset["world"], *preset["test"].get("flags", []), "--set", "world.mutation=0", "--export", str(export)]
    flags += ["--export-day", str(preset["test"]["days"])]
    for source in sources:
        flags += ["--spawn-from", str(source)]
    return flags


def on_day(genomes, day):
    return [one for one in genomes if one["day"] == day]


def original_genome(founders):
    """Founders that differ were mutated as they spawned, and the test would mislabel their descendants."""
    if not founders:
        raise ValueError("no founders were exported on day 1")
    genes = founders[0]["genes"]
    if any(one["genes"] != genes for one in founders):
        raise ValueError("the founders differ, so they were mutated as they spawned")
    return genes


def draw_sample(survivors, size, seed):
    if len(survivors) < size:
        return None
    return random.Random(seed).sample(sorted(survivors, key=lambda one: one["id"]), size)


def evolved_survivors(survivors, original):
    """Rabbits do not age, so a founder can outlive evolution, and it carries the original genome."""
    return [one for one in survivors if one["genes"] != original]


def check_sample(sample, original):
    """A sampled rabbit carrying the original genome would be counted as evolved in the test."""
    if any(one["genes"] == original for one in sample):
        raise ValueError("a sampled survivor carries the original genome, so the groups cannot be told apart")


def evolved_share(survivors, sample, original):
    """Mutation is off in the test, so every survivor carries a founder's exact genes."""
    evolved = [one["genes"] for one in sample]
    counts = {"evolved": 0, "original": 0}
    for one in survivors:
        if one["genes"] in evolved:
            counts["evolved"] += 1
        elif one["genes"] == original:
            counts["original"] += 1
        else:
            raise ValueError(f"rabbit {one['id']} matches neither group, so mutation was on in the test")
    total = counts["evolved"] + counts["original"]
    return {**counts, "share": counts["evolved"] / total if total else None}


def sustained(lines, species):
    """Mean population over every day of the run, so a group that dies out early counts its empty days."""
    return statistics.mean(row["population"].get(species, 0) for row in compare.rows_of(lines))


def gene_means(genomes):
    if not genomes:
        return {}
    return {gene: statistics.mean(one["genes"][gene] for one in genomes) for gene in sorted(genomes[0]["genes"])}


def _median(values):
    return statistics.median(values) if values else None


def report(results, original, export_days, sample_seed=None):
    tested = [seed for seed, result in sorted(results.items()) if result["status"] == "tested"]
    shared = [results[seed]["shared"] for seed in tested]
    known = [one["share"] for one in shared if one["share"] is not None]
    alone = [results[seed]["alone"] for seed in tested]
    last = max(export_days)
    genes = {}
    for gene, start in sorted(original.items()):
        medians = {
            day: _median([results[seed]["genes"][day][gene] for seed in tested if gene in results[seed]["genes"][day]])
            for day in export_days
        }
        ended = [results[seed]["genes"][last][gene] for seed in tested if gene in results[seed]["genes"][last]]
        genes[gene] = {
            "original": start,
            "medians": medians,
            "up": sum(value > start for value in ended),
            "of": len(ended),
        }
    return {
        "seeds": len(results),
        "sampleSeed": sample_seed,
        "tested": len(tested),
        "diedOut": sum(result["status"] == "died out" for result in results.values()),
        "tooFew": sum(result["status"] == "too few" for result in results.values()),
        "shared": {
            "min": min(known, default=None),
            "median": _median(known),
            "max": max(known, default=None),
            "aboveHalf": sum(share > 0.5 for share in known),
            "originalGone": sum(one["original"] == 0 and one["evolved"] > 0 for one in shared),
            "evolvedGone": sum(one["evolved"] == 0 and one["original"] > 0 for one in shared),
            "bothGone": sum(one["share"] is None for one in shared),
        },
        "alone": {
            "evolvedMedian": _median([one["evolved"] for one in alone]),
            "originalMedian": _median([one["original"] for one in alone]),
            "more": sum(one["evolved"] > one["original"] for one in alone),
            "fewer": sum(one["evolved"] < one["original"] for one in alone),
        },
        "genes": genes,
        "exportDays": sorted(export_days),
        "perSeed": {str(seed): results[seed] for seed in sorted(results)},
    }


def _number(value):
    return "-" if value is None else f"{value:.2f}"


def format_text(one, seconds, per_seed):
    shared, alone, days, tested = one["shared"], one["alone"], one["exportDays"], one["tested"]
    out = [
        f"ancestor competition: {one['seeds']} seeds, {tested} tested, sample seed {one['sampleSeed']}",
        f"  {one['diedOut']} died out while evolving, {one['tooFew']} had too few survivors to sample",
        f"  took {int(seconds // 60)}m {int(seconds % 60)}s",
        "",
        "both groups in one world: evolved share of living rabbits on the last day",
        f"  min / median / max             "
        f"{_number(shared['min'])} / {_number(shared['median'])} / {_number(shared['max'])}",
        f"  seeds above one half           {shared['aboveHalf']} of {tested}",
        f"  original genome gone           {shared['originalGone']} of {tested}",
        f"  evolved gone                   {shared['evolvedGone']} of {tested}",
        f"  both gone                      {shared['bothGone']} of {tested}",
        "",
        "each group in a world of its own: rabbits it sustains, mean over every day",
        f"  median                         evolved {_number(alone['evolvedMedian'])},"
        f" original genome {_number(alone['originalMedian'])}",
        f"  evolved sustains more in       {alone['more']} of {tested}",
        f"  evolved sustains fewer in      {alone['fewer']} of {tested}",
        "",
        "evolved genes against the original genome, mean of each seed's survivors",
        "  gene          original  " + "  ".join(f"day {day} median" for day in days) + f"  up on day {days[-1]}",
    ]
    for gene, row in one["genes"].items():
        medians = "  ".join(f"{_number(row['medians'][day]):>{len(f'day {day} median')}}" for day in days)
        out.append(f"  {gene:<12}  {_number(row['original']):>8}  {medians}  {row['up']} of {row['of']}")
    if per_seed:
        out += ["", "per seed: evolved share in one world, rabbits sustained alone by evolved / original"]
        for seed, result in one["perSeed"].items():
            if result["status"] != "tested":
                out.append(f"  {seed:<6} {result['status']}")
                continue
            out.append(
                f"  {seed:<6} share {_number(result['shared']['share'])}"
                f"  alone {_number(result['alone']['evolved'])} / {_number(result['alone']['original'])}"
            )
    return "\n".join(out)


def parse_args(argv):
    parser = argparse.ArgumentParser(
        description="Evolve populations, then set them against the genome they started from."
    )
    parser.add_argument("preset", help="a JSON preset, such as tools/experiments/rabbit-learns-gate.json")
    parser.add_argument("--seeds", help="an inclusive range such as 1-30")
    parser.add_argument("--sample-seed", default="1", help="seeds the draw of each sample, with the world seed")
    parser.add_argument("--jobs", type=int, default=os.cpu_count())
    parser.add_argument("--jar", default=str(compare.DEFAULT_JAR))
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--per-seed", action="store_true")
    return parser.parse_args(argv)


def seeds_of(preset, args):
    return compare.parse_seeds(args.seeds or preset["seeds"])


def test_seed(preset, seed):
    return seed + preset["test"]["seedOffset"]


def original_lines(preset, genes):
    return [json.dumps({"species": preset["species"], "genes": genes})] * preset["sample"]


def read_genomes(path):
    if not path.exists():
        return []
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def _write_lines(path, lines):
    path.write_text("".join(line + "\n" for line in lines), encoding="utf-8")


def _run_all(pool, jar, jobs):
    """jobs maps a key to (label, seed, days, flags). A failed run stops the gate, naming it."""
    futures = {pool.submit(compare.run_world, jar, *job): key for key, job in jobs.items()}
    found = {}
    for future in concurrent.futures.as_completed(futures):
        found[futures[future]] = future.result()
    return found


def _evolve(pool, args, preset, seeds):
    evolve = preset["evolve"]
    return _run_all(
        pool,
        args.jar,
        {
            seed: ("evolve", seed, evolve["days"], evolve_flags(preset, WORK / f"seed-{seed}" / "evolve.jsonl"))
            for seed in seeds
        },
    )


def _prepare(args, preset, seeds, evolved_census, export_days):
    """Reads each seed's export, draws its sample and writes the files its three test runs spawn from."""
    evolve, test = preset["evolve"], preset["test"]
    results, original, jobs = {}, None, {}
    for seed in seeds:
        folder = WORK / f"seed-{seed}"
        _write_lines(folder / "evolve-census.jsonl", evolved_census[seed])
        exported = read_genomes(folder / "evolve.jsonl")
        founders = original_genome(on_day(exported, 1))
        if original is not None and founders != original:
            raise ValueError(f"seed {seed} started from another genome than the seeds before it")
        original = founders
        survivors = on_day(exported, evolve["days"])
        if not survivors:
            results[seed] = {"status": "died out"}
            continue
        sample = draw_sample(evolved_survivors(survivors, original), preset["sample"], f"{args.sample_seed}:{seed}")
        if sample is None:
            results[seed] = {"status": "too few"}
            continue
        check_sample(sample, original)
        _write_lines(folder / "evolved.jsonl", [json.dumps(one) for one in sample])
        _write_lines(folder / "original.jsonl", original_lines(preset, original))
        sources = {
            "shared": [folder / "evolved.jsonl", folder / "original.jsonl"],
            "evolved": [folder / "evolved.jsonl"],
            "original": [folder / "original.jsonl"],
        }
        for group, files in sources.items():
            flags = test_flags(preset, files, folder / f"test-{group}.jsonl")
            jobs[(seed, group)] = (group, test_seed(preset, seed), test["days"], flags)
        results[seed] = {
            "status": "tested",
            "sample": sample,
            "genes": {day: gene_means(on_day(exported, day)) for day in export_days},
        }
    return results, original, jobs


def _score(preset, results, original, test_census):
    test, species = preset["test"], preset["species"]
    for seed, result in results.items():
        if result["status"] != "tested":
            continue
        folder = WORK / f"seed-{seed}"
        for group in ("shared", "evolved", "original"):
            _write_lines(folder / f"test-{group}-census.jsonl", test_census[(seed, group)])
        survivors = on_day(read_genomes(folder / "test-shared.jsonl"), test["days"])
        result["shared"] = evolved_share(survivors, result.pop("sample"), original)
        result["alone"] = {group: sustained(test_census[(seed, group)], species) for group in ("evolved", "original")}


def main(argv):
    args = parse_args(argv)
    preset = json.loads(Path(args.preset).read_text(encoding="utf-8"))
    seeds = seeds_of(preset, args)
    if not Path(args.jar).exists():
        sys.exit(f"no jar at {args.jar}; build it with ./mvnw package -Pskip-frontend")
    started = time.monotonic()
    shutil.rmtree(WORK, ignore_errors=True)
    if WORK.exists():
        sys.exit(f"cannot clear {WORK}; close whatever holds a file in it, or another gate still running")
    for seed in seeds:
        (WORK / f"seed-{seed}").mkdir(parents=True)
    evolve = preset["evolve"]
    export_days = sorted({*evolve["exportDays"], evolve["days"]})
    pool = concurrent.futures.ThreadPoolExecutor(max_workers=args.jobs)
    try:
        evolved_census = _evolve(pool, args, preset, seeds)
        results, original, jobs = _prepare(args, preset, seeds, evolved_census, export_days)
        _score(preset, results, original, _run_all(pool, args.jar, jobs))
    except (compare.RunFailed, ValueError) as failure:
        sys.exit(str(failure))
    finally:
        pool.shutdown(cancel_futures=True)
    one = report(results, original, export_days, args.sample_seed)
    seconds = time.monotonic() - started
    if args.json:
        print(json.dumps({**one, "seconds": round(seconds, 1)}, indent=2))
    else:
        print(format_text(one, seconds, args.per_seed))


if __name__ == "__main__":
    main(sys.argv[1:])
