"""Evolves populations, then sets the evolved genomes against the genome they started from.

It reports and never judges. What a result should show is written on the
roadmap entry that runs it.

    python tools/ancestor_competition.py tools/experiments/rabbit-learns-gate.json
    python tools/ancestor_competition.py tools/experiments/rabbit-learns-gate.json --seeds 1-3

It needs the jar, from ./mvnw package -Pskip-frontend.
"""

import random
import statistics


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
    flags = [*preset["world"], "--set", "world.mutation=0", "--export", str(export)]
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


def gene_means(genomes):
    if not genomes:
        return {}
    return {gene: statistics.mean(one["genes"][gene] for one in genomes) for gene in sorted(genomes[0]["genes"])}


def _median(values):
    return statistics.median(values) if values else None


def report(results, original, export_days):
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
        f"ancestor competition: {one['seeds']} seeds, {tested} tested",
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
        "each group in a world of its own: rabbits it sustains, mean over days alive",
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
