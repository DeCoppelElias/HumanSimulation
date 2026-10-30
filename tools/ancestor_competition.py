"""Evolves populations, then sets the evolved genomes against the genome they started from, or against a control
arm evolved beside them.

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


def arms(preset):
    """The evolving arm, and a control evolved beside it when the preset has one."""
    found = {"evolve": preset["evolve"]["flags"]}
    if "control" in preset:
        found["control"] = preset["control"]["flags"]
    return found


def evolve_flags(preset, export, flags=None):
    """Founders spawn with mutation off, so they are identical, and mutation starts once day 1 is complete."""
    evolve = preset["evolve"]
    out = [
        *preset["world"],
        "--spawn", f"{preset['species']}={evolve['founders']}",
        *(evolve["flags"] if flags is None else flags),
        "--set", "world.mutation=0",
        "--set-at", "1:world.mutation=1",
        "--export", str(export),
    ]
    for day in sorted({1, *evolve["exportDays"], evolve["days"]}):
        out += ["--export-day", str(day)]
    return out


def test_flags(preset, sources, export):
    test = preset["test"]
    flags = [*test.get("world", preset["world"]), *test.get("flags", []), "--set", "world.mutation=0", "--export", str(export)]
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


def check_apart(sample, other):
    """Two samples sharing a genome would put its descendants in both groups."""
    genes = [one["genes"] for one in other]
    if any(one["genes"] in genes for one in sample):
        raise ValueError("the two samples share a genome, so the groups cannot be told apart")


def draw_partners(survivors, partners, label):
    """The other species the evolving arm lived beside, drawn from its last day, so the test meets what it evolved against."""
    drawn = []
    for species, count in sorted(partners.items()):
        sample = draw_sample(survivors.get(species, []), count, f"{label}:{species}")
        if sample is None:
            return None
        drawn += sample
    return drawn


def sources_of(folder, partners):
    """The files each test world spawns from. Every world gets the same partners."""
    extra = [folder / "partners.jsonl"] if partners else []
    return {
        "shared": [folder / "evolved.jsonl", folder / "original.jsonl", *extra],
        "evolved": [folder / "evolved.jsonl", *extra],
        "original": [folder / "original.jsonl", *extra],
    }


def lost_partners(census, partners):
    """Species the evolving arm needs beside it, such as the wolves that teach it, gone by its last day."""
    last = compare.rows_of(census)[-1]["population"]
    return [one for one in partners if last.get(one, 0) == 0]


def alive_on(census, species, day):
    """A day past the end of the run counts as gone."""
    return any(row["day"] == day and row["population"].get(species, 0) > 0 for row in compare.rows_of(census))


def evolved_share(survivors, sample, others):
    """Mutation is off in the test, so every survivor carries a founder's exact genes. others are the comparison group's genomes."""
    evolved = [one["genes"] for one in sample]
    counts = {"evolved": 0, "original": 0}
    for one in survivors:
        if one["genes"] in evolved:
            counts["evolved"] += 1
        elif one["genes"] in others:
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


def _day_medians(results, tested, key, gene, export_days):
    return {
        day: _median([results[seed][key][day][gene] for seed in tested if gene in results[seed][key][day]])
        for day in export_days
    }


def report(results, original, export_days, sample_seed=None, against="original genome", watch=None):
    tested = [seed for seed, result in sorted(results.items()) if result["status"] == "tested"]
    shared = [results[seed]["shared"] for seed in tested]
    known = [one["share"] for one in shared if one["share"] is not None]
    alone = [results[seed]["alone"] for seed in tested]
    controlled = [seed for seed in tested if results[seed].get("controlGenes")]
    last = max(export_days)
    genes = {}
    for gene, start in sorted(original.items()):
        ended = [results[seed]["genes"][last][gene] for seed in tested if gene in results[seed]["genes"][last]]
        genes[gene] = {
            "original": start,
            "medians": _day_medians(results, tested, "genes", gene, export_days),
            "up": sum(value > start for value in ended),
            "of": len(ended),
        }
        if controlled:
            genes[gene]["controlMedians"] = _day_medians(results, controlled, "controlGenes", gene, export_days)
    found = {
        "seeds": len(results),
        "sampleSeed": sample_seed,
        "against": against,
        "tested": len(tested),
        "diedOut": sum(result["status"] == "died out" for result in results.values()),
        "partnerDiedOut": sum(result["status"] == "partner died out" for result in results.values()),
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
    if watch is not None:
        found["watch"] = {
            **watch,
            "counts": {
                group: sum(results[seed].get("watch", {}).get(group, False) for seed in tested)
                for group in ("shared", "evolved", "original")
            },
        }
    return found


def _number(value):
    return "-" if value is None else f"{value:.2f}"


def format_text(one, seconds, per_seed):
    shared, alone, days, tested = one["shared"], one["alone"], one["exportDays"], one["tested"]
    against = one.get("against", "original genome")
    start = "original genome" if against == "original genome" else "genome both arms started from"
    width = max([12] + [len(gene) for gene in one["genes"]])
    columns = [("medians", f"day {day} median", day) for day in days]
    if any("controlMedians" in row for row in one["genes"].values()):
        columns += [("controlMedians", f"control day {day} median", day) for day in days]
    out = [
        f"ancestor competition: {one['seeds']} seeds, {tested} tested, sample seed {one['sampleSeed']}",
        f"  {one['diedOut']} died out while evolving, {one['partnerDiedOut']} lost a species they evolved beside,"
        f" {one['tooFew']} had too few survivors to sample",
        f"  took {int(seconds // 60)}m {int(seconds % 60)}s",
        "",
        "both groups in one world: evolved share of living rabbits on the last day",
        f"  min / median / max             "
        f"{_number(shared['min'])} / {_number(shared['median'])} / {_number(shared['max'])}",
        f"  seeds above one half           {shared['aboveHalf']} of {tested}",
        f"  {against + ' gone':<31}{shared['originalGone']} of {tested}",
        f"  evolved gone                   {shared['evolvedGone']} of {tested}",
        f"  both gone                      {shared['bothGone']} of {tested}",
        "",
        "each group in a world of its own: rabbits it sustains, mean over every day",
        f"  median                         evolved {_number(alone['evolvedMedian'])},"
        f" {against} {_number(alone['originalMedian'])}",
        f"  evolved sustains more in       {alone['more']} of {tested}",
        f"  evolved sustains fewer in      {alone['fewer']} of {tested}",
    ]
    if "watch" in one:
        watch = one["watch"]
        counts = watch["counts"]
        out += [
            "",
            f"{watch['species']} alive on day {watch['day']} in test worlds: shared {counts['shared']} of {tested},"
            f" evolved {counts['evolved']}, {against} {counts['original']}",
        ]
    out += [
        "",
        f"evolved genes against the {start}, mean of each seed's survivors",
        f"  {'gene':<{width}}  original  " + "  ".join(title for _, title, _ in columns) + f"  up on day {days[-1]}",
    ]
    for gene, row in one["genes"].items():
        medians = "  ".join(f"{_number(row[key][day]):>{len(title)}}" for key, title, day in columns)
        out.append(f"  {gene:<{width}}  {_number(row['original']):>8}  {medians}  {row['up']} of {row['of']}")
    if per_seed:
        out += ["", f"per seed: evolved share in one world, rabbits sustained alone by evolved / {against}"]
        for seed, result in one["perSeed"].items():
            if result["status"] != "tested":
                arm = f" in the {result['arm']}" if "arm" in result else ""
                out.append(f"  {seed:<6} {result['status']}{arm}")
                continue
            out.append(
                f"  {seed:<6} share {_number(result['shared']['share'])}"
                f"  alone {_number(result['alone']['evolved'])} / {_number(result['alone']['original'])}"
            )
    return "\n".join(out)


def parse_args(argv):
    parser = argparse.ArgumentParser(
        description="Evolve populations, then set them against the genome they started from or a control arm."
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


def read_genomes(path, species):
    """The run exports every creature with genes, and the gate compares one species."""
    if not path.exists():
        return []
    lines = [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    return [one for one in lines if one["species"] == species]


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
            (seed, arm): (arm, seed, evolve["days"], evolve_flags(preset, WORK / f"seed-{seed}" / f"{arm}.jsonl", flags))
            for seed in seeds
            for arm, flags in arms(preset).items()
        },
    )


def _prepare(args, preset, seeds, evolved_census, export_days):
    """Reads each seed's export, draws its sample and writes the files its three test runs spawn from."""
    evolve, test, species = preset["evolve"], preset["test"], preset["species"]
    results, original, jobs = {}, None, {}
    for seed in seeds:
        folder = WORK / f"seed-{seed}"
        for arm in arms(preset):
            _write_lines(folder / f"{arm}-census.jsonl", evolved_census[(seed, arm)])
        exported = read_genomes(folder / "evolve.jsonl", species)
        founders = original_genome(on_day(exported, 1))
        if original is not None and founders != original:
            raise ValueError(f"seed {seed} started from another genome than the seeds before it")
        original = founders
        survivors = on_day(exported, evolve["days"])
        if not survivors:
            results[seed] = {"status": "died out"}
            continue
        if lost_partners(evolved_census[(seed, "evolve")], evolve.get("mustSurvive", [])):
            results[seed] = {"status": "partner died out"}
            continue
        sample = draw_sample(evolved_survivors(survivors, original), preset["sample"], f"{args.sample_seed}:{seed}")
        if sample is None:
            results[seed] = {"status": "too few"}
            continue
        check_sample(sample, original)
        if "control" in preset:
            control = read_genomes(folder / "control.jsonl", species)
            if original_genome(on_day(control, 1)) != original:
                raise ValueError(f"seed {seed}'s control started from another genome than its evolving arm")
            kept = on_day(control, evolve["days"])
            if not kept:
                results[seed] = {"status": "died out", "arm": "control"}
                continue
            other = draw_sample(evolved_survivors(kept, original), preset["sample"], f"{args.sample_seed}:{seed}:control")
            if other is None:
                results[seed] = {"status": "too few", "arm": "control"}
                continue
            check_apart(sample, other)
            other_lines = [json.dumps(one) for one in other]
            others = [one["genes"] for one in other]
            control_genes = {day: gene_means(on_day(control, day)) for day in export_days}
        else:
            other_lines = original_lines(preset, original)
            others = [original]
            control_genes = None
        partners = test.get("partners", {})
        if partners:
            survivors_by_species = {
                one: on_day(read_genomes(folder / "evolve.jsonl", one), evolve["days"]) for one in partners
            }
            drawn = draw_partners(survivors_by_species, partners, f"{args.sample_seed}:{seed}")
            if drawn is None:
                results[seed] = {"status": "too few", "arm": "partners"}
                continue
            _write_lines(folder / "partners.jsonl", [json.dumps(one) for one in drawn])
        _write_lines(folder / "evolved.jsonl", [json.dumps(one) for one in sample])
        _write_lines(folder / "original.jsonl", other_lines)
        for group, files in sources_of(folder, bool(partners)).items():
            flags = test_flags(preset, files, folder / f"test-{group}.jsonl")
            jobs[(seed, group)] = (group, test_seed(preset, seed), test["days"], flags)
        results[seed] = {
            "status": "tested",
            "sample": sample,
            "others": others,
            "genes": {day: gene_means(on_day(exported, day)) for day in export_days},
        }
        if control_genes is not None:
            results[seed]["controlGenes"] = control_genes
    return results, original, jobs


def _score(preset, results, test_census):
    test, species = preset["test"], preset["species"]
    for seed, result in results.items():
        if result["status"] != "tested":
            continue
        folder = WORK / f"seed-{seed}"
        for group in ("shared", "evolved", "original"):
            _write_lines(folder / f"test-{group}-census.jsonl", test_census[(seed, group)])
        survivors = on_day(read_genomes(folder / "test-shared.jsonl", species), test["days"])
        result["shared"] = evolved_share(survivors, result.pop("sample"), result.pop("others"))
        result["alone"] = {group: sustained(test_census[(seed, group)], species) for group in ("evolved", "original")}
        if "watch" in test:
            watch = test["watch"]
            result["watch"] = {
                group: alive_on(test_census[(seed, group)], watch["species"], watch["day"])
                for group in ("shared", "evolved", "original")
            }


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
        _score(preset, results, _run_all(pool, args.jar, jobs))
    except (compare.RunFailed, ValueError) as failure:
        sys.exit(str(failure))
    finally:
        pool.shutdown(cancel_futures=True)
    against = "control" if "control" in preset else "original genome"
    one = report(results, original, export_days, args.sample_seed, against, preset["test"].get("watch"))
    seconds = time.monotonic() - started
    if args.json:
        print(json.dumps({**one, "seconds": round(seconds, 1)}, indent=2))
    else:
        print(format_text(one, seconds, args.per_seed))


if __name__ == "__main__":
    main(sys.argv[1:])
