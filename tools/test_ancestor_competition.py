"""Tests for ancestor_competition.py. Run with: python tools/test_ancestor_competition.py"""

import importlib.util
import json
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
spec = importlib.util.spec_from_file_location(
    "ancestor_competition", Path(__file__).with_name("ancestor_competition.py")
)
ac = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ac)

ORIGINAL = {"near.Plant": 0.2, "whim": 2.0}

PRESET = {
    "seeds": "1-30",
    "species": "rabbit",
    "world": ["--width", "40", "--height", "40", "--set", "world.grassPerDrop=28"],
    "evolve": {"days": 1000, "founders": 20, "exportDays": [500], "flags": ["--gene", "rabbit.near.Plant=0.2"]},
    "sample": 10,
    "test": {"days": 500, "seedOffset": 1000},
}


def genome(id_, genes, day=1000):
    return {"seed": 1, "day": day, "species": "rabbit", "id": id_, "genes": dict(genes)}


def evolved(n):
    return {"near.Plant": 1.0 + n / 100, "whim": 1.0}


class EvolveFlagsTest(unittest.TestCase):
    def test_founders_spawn_identical_and_mutate_from_day_one(self):
        flags = ac.evolve_flags(PRESET, Path("out.jsonl"))
        self.assertLess(flags.index("world.mutation=0"), flags.index("1:world.mutation=1"))
        self.assertIn("rabbit=20", flags)
        self.assertLess(flags.index("world.grassPerDrop=28"), flags.index("rabbit.near.Plant=0.2"))

    def test_exports_day_one_each_export_day_and_the_last(self):
        flags = ac.evolve_flags(PRESET, Path("out.jsonl"))
        days = [flags[i + 1] for i, flag in enumerate(flags) if flag == "--export-day"]
        self.assertEqual(days, ["1", "500", "1000"])


class OriginalGenomeTest(unittest.TestCase):
    def test_identical_founders_give_their_genes(self):
        self.assertEqual(ac.original_genome([genome(1, ORIGINAL, 1), genome(2, ORIGINAL, 1)]), ORIGINAL)

    def test_founders_that_differ_are_refused(self):
        with self.assertRaisesRegex(ValueError, "mutated"):
            ac.original_genome([genome(1, ORIGINAL, 1), genome(2, {"near.Plant": 0.3, "whim": 2.0}, 1)])


class SampleTest(unittest.TestCase):
    SURVIVORS = [genome(i, evolved(i)) for i in range(20)]

    def test_the_same_seed_draws_the_same_sample(self):
        self.assertEqual(ac.draw_sample(self.SURVIVORS, 10, "1:7"), ac.draw_sample(self.SURVIVORS, 10, "1:7"))

    def test_another_seed_draws_another(self):
        self.assertNotEqual(ac.draw_sample(self.SURVIVORS, 10, "1:7"), ac.draw_sample(self.SURVIVORS, 10, "1:8"))

    def test_the_order_survivors_arrive_in_does_not_matter(self):
        self.assertEqual(
            ac.draw_sample(self.SURVIVORS, 10, "1:7"), ac.draw_sample(list(reversed(self.SURVIVORS)), 10, "1:7")
        )

    def test_too_few_draw_nothing(self):
        self.assertIsNone(ac.draw_sample(self.SURVIVORS[:9], 10, "1:7"))

    def test_survivors_come_from_the_last_day_only(self):
        exported = [genome(1, evolved(1), 500), genome(2, evolved(2), 1000)]
        self.assertEqual([one["id"] for one in ac.on_day(exported, 1000)], [2])


class ShareTest(unittest.TestCase):
    SAMPLE = [genome(i, evolved(i)) for i in range(3)]

    def test_groups_are_matched_by_genes_not_id(self):
        survivors = [genome(0, ORIGINAL, 500), genome(1, ORIGINAL, 500), genome(77, evolved(2), 500)]
        self.assertEqual(
            ac.evolved_share(survivors, self.SAMPLE, [ORIGINAL]), {"evolved": 1, "original": 2, "share": 1 / 3}
        )

    def test_a_rabbit_in_neither_group_is_refused(self):
        with self.assertRaisesRegex(ValueError, "neither group"):
            ac.evolved_share([genome(5, {"near.Plant": 3.0, "whim": 1.0}, 500)], self.SAMPLE, [ORIGINAL])

    def test_no_survivors_has_no_share(self):
        self.assertIsNone(ac.evolved_share([], self.SAMPLE, [ORIGINAL])["share"])


def tested(share, evolved_alone, original_alone, genes_500=None, genes_1000=None):
    return {
        "status": "tested",
        "shared": {"evolved": 0, "original": 0, "share": share},
        "alone": {"evolved": evolved_alone, "original": original_alone},
        "genes": {500: genes_500 or {"near.Plant": 0.5}, 1000: genes_1000 or {"near.Plant": 0.9}},
    }


class ReportTest(unittest.TestCase):
    RESULTS = {
        1: tested(0.8, 30, 20),
        2: tested(0.5, 20, 20),
        3: tested(None, 10, 20),
        4: tested(0.6, 25, 26, genes_1000={"near.Plant": 0.1}),
        5: {"status": "died out"},
        6: {"status": "too few"},
    }

    def test_counts_seeds_by_status(self):
        report = ac.report(self.RESULTS, {"near.Plant": 0.2}, [500, 1000])
        self.assertEqual((report["seeds"], report["tested"], report["diedOut"], report["tooFew"]), (6, 4, 1, 1))

    def test_a_share_counts_only_above_one_half(self):
        self.assertEqual(ac.report(self.RESULTS, {"near.Plant": 0.2}, [500, 1000])["shared"]["aboveHalf"], 2)

    def test_alone_counts_more_and_fewer_with_ties_in_neither(self):
        alone = ac.report(self.RESULTS, {"near.Plant": 0.2}, [500, 1000])["alone"]
        self.assertEqual((alone["more"], alone["fewer"]), (1, 2))

    def test_genes_count_seeds_above_the_original_on_the_last_day(self):
        genes = ac.report(self.RESULTS, {"near.Plant": 0.2}, [500, 1000])["genes"]["near.Plant"]
        self.assertEqual((genes["original"], genes["up"], genes["of"]), (0.2, 3, 4))
        self.assertEqual(genes["medians"], {500: 0.5, 1000: 0.9})


class TextTest(unittest.TestCase):
    def test_names_each_section_without_a_verdict(self):
        text = ac.format_text(ac.report(ReportTest.RESULTS, {"near.Plant": 0.2}, [500, 1000]), 12.0, per_seed=False)
        for heading in ("both groups in one world", "each group in a world of its own", "evolved genes"):
            self.assertIn(heading, text)
        self.assertNotIn("pass", text.lower())
        self.assertNotIn("fail", text.lower())

    def test_the_gene_column_fits_the_longest_name(self):
        long = "hungry.near.takes.Plant"
        results = {1: tested(0.8, 30, 20, {long: 0.5, "whim": 1.0}, {long: 0.9, "whim": 1.0})}
        text = ac.format_text(ac.report(results, {long: 0.2, "whim": 2.0}, [500, 1000]), 1.0, per_seed=False)
        rows = text.split("evolved genes against the original genome")[1].splitlines()[1:4]
        self.assertEqual(len({row.index("original") + len("original") for row in rows[:1]}
                             | {row.index("0.20") + 4 for row in rows if long in row}
                             | {row.index("2.00") + 4 for row in rows if "whim" in row}), 1)


class TestFlagsTest(unittest.TestCase):
    def test_spawns_only_from_its_files_with_mutation_off_and_exports_its_last_day(self):
        flags = ac.test_flags(PRESET, [Path("e.jsonl"), Path("o.jsonl")], Path("t.jsonl"))
        self.assertIn(("--set", "world.mutation=0"), list(zip(flags, flags[1:])))
        self.assertEqual([flags[i + 1] for i, flag in enumerate(flags) if flag == "--spawn-from"], ["e.jsonl", "o.jsonl"])
        self.assertEqual([flags[i + 1] for i, flag in enumerate(flags) if flag == "--export-day"], ["500"])
        self.assertEqual(flags[flags.index("--export") + 1], "t.jsonl")
        self.assertNotIn("--spawn", flags)
        self.assertNotIn("--gene", flags)

    def test_its_own_flags_follow_the_world_and_stay_out_of_evolution(self):
        preset = {**PRESET, "test": {**PRESET["test"], "flags": ["--set", "world.grassDropInterval=3"]}}
        flags = ac.test_flags(preset, [Path("e.jsonl")], Path("t.jsonl"))
        self.assertLess(flags.index("world.grassPerDrop=28"), flags.index("world.grassDropInterval=3"))
        self.assertNotIn("world.grassDropInterval=3", ac.evolve_flags(preset, Path("out.jsonl")))

    def test_a_test_world_replaces_the_evolution_world(self):
        preset = {**PRESET, "test": {**PRESET["test"], "world": ["--width", "20", "--height", "20"]}}
        flags = ac.test_flags(preset, [Path("e.jsonl")], Path("t.jsonl"))
        self.assertEqual(flags[:4], ["--width", "20", "--height", "20"])
        self.assertNotIn("40", flags)


class SampleOfTheOriginalTest(unittest.TestCase):
    def test_a_sample_holding_the_original_genome_is_refused(self):
        with self.assertRaisesRegex(ValueError, "original genome"):
            ac.check_sample([genome(1, evolved(1)), genome(2, ORIGINAL)], ORIGINAL)

    def test_a_sample_without_it_passes(self):
        ac.check_sample([genome(1, evolved(1))], ORIGINAL)


class EvolvedSurvivorsTest(unittest.TestCase):
    def test_a_founder_still_alive_is_not_evolved(self):
        survivors = [genome(1, ORIGINAL), genome(2, evolved(2)), genome(3, ORIGINAL)]
        self.assertEqual([one["id"] for one in ac.evolved_survivors(survivors, ORIGINAL)], [2])


class SampleSeedTest(unittest.TestCase):
    def test_the_report_records_the_sample_seed(self):
        one = ac.report(ReportTest.RESULTS, {"near.Plant": 0.2}, [500, 1000], "9")
        self.assertEqual(one["sampleSeed"], "9")
        self.assertIn("sample seed 9", ac.format_text(one, 1.0, per_seed=False))


class SustainedTest(unittest.TestCase):
    def test_days_after_dying_out_count_as_none(self):
        lines = [
            '{"day":1,"population":{"rabbit":10}}',
            '{"day":2,"population":{"rabbit":10}}',
            '{"day":3,"population":{"rabbit":0}}',
            '{"day":4,"population":{"rabbit":0}}',
        ]
        self.assertEqual(ac.sustained(lines, "rabbit"), 5)


class PlanTest(unittest.TestCase):
    def test_seeds_on_the_command_line_override_the_preset(self):
        args = ac.parse_args(["preset.json", "--seeds", "3-4"])
        self.assertEqual(ac.seeds_of(PRESET, args), [3, 4])

    def test_the_preset_gives_seeds_otherwise(self):
        self.assertEqual(len(ac.seeds_of(PRESET, ac.parse_args(["preset.json"]))), 30)

    def test_a_test_runs_on_its_own_seed(self):
        self.assertEqual(ac.test_seed(PRESET, 7), 1007)

    def test_the_original_file_holds_size_copies_by_species_and_genes(self):
        lines = ac.original_lines(PRESET, ORIGINAL)
        self.assertEqual(len(lines), 10)
        self.assertEqual(lines[0], '{"species": "rabbit", "genes": {"near.Plant": 0.2, "whim": 2.0}}')


class SpeciesTest(unittest.TestCase):
    def test_reading_keeps_only_the_named_species(self):
        path = Path(ac.WORK.parent / "species-test.jsonl")
        path.parent.mkdir(parents=True, exist_ok=True)
        wolf = {**genome(9, {"near.Meat": 1.0}), "species": "wolf"}
        path.write_text("\n".join(json.dumps(one) for one in [genome(1, ORIGINAL), wolf]) + "\n", encoding="utf-8")
        try:
            self.assertEqual([one["id"] for one in ac.read_genomes(path, "rabbit")], [1])
        finally:
            path.unlink()


CONTROLLED = {**PRESET, "evolve": {**PRESET["evolve"], "flags": ["--spawn", "wolf=24"]}, "control": {"flags": []}}


class ControlTest(unittest.TestCase):
    def test_each_arm_evolves_with_its_own_flags(self):
        arms = ac.arms(CONTROLLED)
        self.assertEqual(list(arms), ["evolve", "control"])
        self.assertIn("wolf=24", ac.evolve_flags(CONTROLLED, Path("e.jsonl"), arms["evolve"]))
        self.assertNotIn("wolf=24", ac.evolve_flags(CONTROLLED, Path("c.jsonl"), arms["control"]))

    def test_without_a_control_only_one_arm_evolves(self):
        self.assertEqual(list(ac.arms(PRESET)), ["evolve"])

    def test_a_share_against_a_control_sample_matches_its_genomes(self):
        control = [genome(50 + i, {"near.Plant": 2.0 + i, "whim": 1.0}) for i in range(2)]
        survivors = [genome(1, evolved(0), 500), genome(2, control[1]["genes"], 500)]
        self.assertEqual(
            ac.evolved_share(survivors, ShareTest.SAMPLE, [one["genes"] for one in control]),
            {"evolved": 1, "original": 1, "share": 0.5},
        )

    def test_samples_that_share_a_genome_are_refused(self):
        with self.assertRaisesRegex(ValueError, "cannot be told apart"):
            ac.check_apart([genome(1, evolved(1))], [genome(2, evolved(1))])

    def test_the_text_names_the_control(self):
        one = ac.report(ReportTest.RESULTS, {"near.Plant": 0.2}, [500, 1000], "1", against="control")
        text = ac.format_text(one, 1.0, per_seed=False)
        self.assertIn("control", text)
        self.assertNotIn("original genome", text)

    def test_per_seed_rows_name_the_control_and_the_arm_that_failed(self):
        results = {**ReportTest.RESULTS, 9: {"status": "died out", "arm": "control"}}
        text = ac.format_text(ac.report(results, {"near.Plant": 0.2}, [500, 1000], against="control"), 1.0, True)
        self.assertIn("evolved / control", text)
        self.assertIn("died out in the control", text)


class PartnerTest(unittest.TestCase):
    CENSUS = ['{"day":1,"population":{"rabbit":20,"wolf":4}}', '{"day":2,"population":{"rabbit":18,"wolf":0}}']

    def test_a_partner_gone_on_the_last_day_is_named(self):
        self.assertEqual(ac.lost_partners(self.CENSUS, ["wolf"]), ["wolf"])

    def test_a_partner_alive_is_not(self):
        self.assertEqual(ac.lost_partners(self.CENSUS[:1], ["wolf"]), [])

    def test_the_report_counts_seeds_that_lost_one(self):
        one = ac.report({9: {"status": "partner died out"}, **ReportTest.RESULTS}, {"near.Plant": 0.2}, [500, 1000])
        self.assertEqual(one["partnerDiedOut"], 1)
        self.assertIn("1 lost a species they evolved beside", ac.format_text(one, 1.0, per_seed=False))


class ControlGenesTest(unittest.TestCase):
    def test_control_medians_sit_beside_the_evolved(self):
        results = {
            7: {
                **tested(0.6, 10, 9, {"near.Plant": 1.0}, {"near.Plant": 2.0}),
                "controlGenes": {500: {"near.Plant": 0.5}, 1000: {"near.Plant": 0.7}},
            }
        }
        one = ac.report(results, {"near.Plant": 0.2}, [500, 1000])
        self.assertEqual(one["genes"]["near.Plant"]["controlMedians"], {500: 0.5, 1000: 0.7})
        self.assertIn("control day 1000 median", ac.format_text(one, 1.0, per_seed=False))

    def test_without_a_control_there_is_no_column(self):
        one = ac.report(ReportTest.RESULTS, {"near.Plant": 0.2}, [500, 1000])
        self.assertNotIn("control day", ac.format_text(one, 1.0, per_seed=False))
        self.assertNotIn("below control", ac.format_text(one, 1.0, per_seed=False))

    def test_counts_seeds_below_the_start_and_below_their_control(self):
        def seed(evolved_end, control_end):
            return {
                **tested(0.5, 10, 10, {"g": 0.0}, {"g": evolved_end}),
                "controlGenes": {500: {"g": 0.0}, 1000: {"g": control_end}},
            }

        results = {1: seed(-1.0, -0.5), 2: seed(-0.6, -0.8), 3: seed(-0.4, -0.3), 4: seed(-0.5, -0.4)}
        row = ac.report(results, {"g": -0.5}, [500, 1000])["genes"]["g"]
        self.assertEqual((row["up"], row["down"], row["belowControl"]), (1, 2, 3))
        text = ac.format_text(ac.report(results, {"g": -0.5}, [500, 1000]), 1.0, per_seed=False)
        self.assertIn("down on day 1000", text)
        self.assertIn("below control on day 1000", text)


class WatchTest(unittest.TestCase):
    CENSUS = ['{"day":250,"population":{"rabbit":20,"wolf":2}}', '{"day":251,"population":{"rabbit":20,"wolf":0}}']

    def test_alive_on_the_day_counts(self):
        self.assertTrue(ac.alive_on(self.CENSUS, "wolf", 250))

    def test_gone_by_the_day_does_not(self):
        self.assertFalse(ac.alive_on(self.CENSUS, "wolf", 251))

    def test_the_report_counts_test_worlds_per_group(self):
        results = {7: {**tested(0.6, 10, 9), "watch": {"shared": True, "evolved": False, "original": True}}}
        one = ac.report(results, {"near.Plant": 0.2}, [500, 1000], watch={"species": "wolf", "day": 250})
        self.assertEqual(one["watch"]["counts"], {"shared": 1, "evolved": 0, "original": 1})
        self.assertIn("wolf alive on day 250", ac.format_text(one, 1.0, per_seed=False))


class PartnersTest(unittest.TestCase):
    WOLVES = [{**genome(100 + i, {"near.Meat": 1.0 + i / 10}), "species": "wolf"} for i in range(6)]

    def test_each_partner_species_gives_its_count(self):
        drawn = ac.draw_partners({"wolf": self.WOLVES}, {"wolf": 4}, "1:7")
        self.assertEqual(len(drawn), 4)
        self.assertTrue(all(one["species"] == "wolf" for one in drawn))

    def test_the_same_label_draws_the_same_partners(self):
        self.assertEqual(
            ac.draw_partners({"wolf": self.WOLVES}, {"wolf": 4}, "1:7"),
            ac.draw_partners({"wolf": list(reversed(self.WOLVES))}, {"wolf": 4}, "1:7"),
        )

    def test_too_few_of_a_partner_draws_nothing(self):
        self.assertIsNone(ac.draw_partners({"wolf": self.WOLVES[:3]}, {"wolf": 4}, "1:7"))

    def test_every_test_world_spawns_the_partners(self):
        sources = ac.sources_of(Path("seed-1"), partners=True)
        for group in ("shared", "evolved", "original"):
            self.assertIn(Path("seed-1") / "partners.jsonl", sources[group])
        self.assertEqual(sources["evolved"][0], Path("seed-1") / "evolved.jsonl")

    def test_without_partners_no_world_spawns_them(self):
        sources = ac.sources_of(Path("seed-1"), partners=False)
        self.assertEqual(sources["shared"], [Path("seed-1") / "evolved.jsonl", Path("seed-1") / "original.jsonl"])
        self.assertEqual(sources["original"], [Path("seed-1") / "original.jsonl"])


GOLDEN_EVOLVE = [
    "--width", "40", "--height", "40", "--set", "world.grassPerDrop=28",
    "--spawn", "rabbit=20",
    "--gene", "rabbit.near.Plant=1.6", "--gene", "rabbit.hungry.near.Plant=0.5", "--gene", "rabbit.whim=2",
    "--set", "world.grassDropInterval=1",
    "--set-at", "400:world.grassDropInterval=2", "--set-at", "800:world.grassDropInterval=3",
    "--set-at", "1200:world.grassDropInterval=5", "--set-at", "1600:world.grassDropInterval=7",
    "--set", "world.mutation=0",
    "--set-at", "1:world.mutation=1",
    "--export", "e.jsonl",
    "--export-day", "1", "--export-day", "1000", "--export-day", "2000",
]

GOLDEN_TEST = [
    "--width", "40", "--height", "40", "--set", "world.grassPerDrop=28",
    "--set", "world.grassDropInterval=3",
    "--set", "world.mutation=0",
    "--export", "t.jsonl",
    "--export-day", "500",
    "--spawn-from", "a.jsonl", "--spawn-from", "b.jsonl",
]


class EntryNineTest(unittest.TestCase):
    PRESET = json.loads((Path(__file__).parent / "experiments" / "rabbit-learns-gate.json").read_text(encoding="utf-8"))

    def test_flags_are_unchanged(self):
        self.assertEqual(ac.evolve_flags(self.PRESET, Path("e.jsonl")), GOLDEN_EVOLVE)
        self.assertEqual(ac.test_flags(self.PRESET, [Path("a.jsonl"), Path("b.jsonl")], Path("t.jsonl")), GOLDEN_TEST)
        self.assertEqual(list(ac.arms(self.PRESET)), ["evolve"])


if __name__ == "__main__":
    unittest.main()
