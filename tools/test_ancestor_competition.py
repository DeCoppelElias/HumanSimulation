"""Tests for ancestor_competition.py. Run with: python tools/test_ancestor_competition.py"""

import importlib.util
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
spec = importlib.util.spec_from_file_location(
    "ancestor_competition", Path(__file__).with_name("ancestor_competition.py")
)
ac = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ac)

ORIGINAL = {"grassPull": 0.2, "whim": 2.0}

PRESET = {
    "seeds": "1-30",
    "species": "rabbit",
    "world": ["--width", "40", "--height", "40", "--set", "world.grassPerDrop=28"],
    "evolve": {"days": 1000, "founders": 20, "exportDays": [500], "flags": ["--gene", "rabbit.grassPull=0.2"]},
    "sample": 10,
    "test": {"days": 500, "seedOffset": 1000},
}


def genome(id_, genes, day=1000):
    return {"seed": 1, "day": day, "species": "rabbit", "id": id_, "genes": dict(genes)}


def evolved(n):
    return {"grassPull": 1.0 + n / 100, "whim": 1.0}


class EvolveFlagsTest(unittest.TestCase):
    def test_founders_spawn_identical_and_mutate_from_day_one(self):
        flags = ac.evolve_flags(PRESET, Path("out.jsonl"))
        self.assertLess(flags.index("world.mutation=0"), flags.index("1:world.mutation=1"))
        self.assertIn("rabbit=20", flags)
        self.assertLess(flags.index("world.grassPerDrop=28"), flags.index("rabbit.grassPull=0.2"))

    def test_exports_day_one_each_export_day_and_the_last(self):
        flags = ac.evolve_flags(PRESET, Path("out.jsonl"))
        days = [flags[i + 1] for i, flag in enumerate(flags) if flag == "--export-day"]
        self.assertEqual(days, ["1", "500", "1000"])


class OriginalGenomeTest(unittest.TestCase):
    def test_identical_founders_give_their_genes(self):
        self.assertEqual(ac.original_genome([genome(1, ORIGINAL, 1), genome(2, ORIGINAL, 1)]), ORIGINAL)

    def test_founders_that_differ_are_refused(self):
        with self.assertRaisesRegex(ValueError, "mutated"):
            ac.original_genome([genome(1, ORIGINAL, 1), genome(2, {"grassPull": 0.3, "whim": 2.0}, 1)])


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
            ac.evolved_share(survivors, self.SAMPLE, ORIGINAL), {"evolved": 1, "original": 2, "share": 1 / 3}
        )

    def test_a_rabbit_in_neither_group_is_refused(self):
        with self.assertRaisesRegex(ValueError, "neither group"):
            ac.evolved_share([genome(5, {"grassPull": 3.0, "whim": 1.0}, 500)], self.SAMPLE, ORIGINAL)

    def test_no_survivors_has_no_share(self):
        self.assertIsNone(ac.evolved_share([], self.SAMPLE, ORIGINAL)["share"])


def tested(share, evolved_alone, original_alone, genes_500=None, genes_1000=None):
    return {
        "status": "tested",
        "shared": {"evolved": 0, "original": 0, "share": share},
        "alone": {"evolved": evolved_alone, "original": original_alone},
        "genes": {500: genes_500 or {"grassPull": 0.5}, 1000: genes_1000 or {"grassPull": 0.9}},
    }


class ReportTest(unittest.TestCase):
    RESULTS = {
        1: tested(0.8, 30, 20),
        2: tested(0.5, 20, 20),
        3: tested(None, 10, 20),
        4: tested(0.6, 25, 26, genes_1000={"grassPull": 0.1}),
        5: {"status": "died out"},
        6: {"status": "too few"},
    }

    def test_counts_seeds_by_status(self):
        report = ac.report(self.RESULTS, {"grassPull": 0.2}, [500, 1000])
        self.assertEqual((report["seeds"], report["tested"], report["diedOut"], report["tooFew"]), (6, 4, 1, 1))

    def test_a_share_counts_only_above_one_half(self):
        self.assertEqual(ac.report(self.RESULTS, {"grassPull": 0.2}, [500, 1000])["shared"]["aboveHalf"], 2)

    def test_alone_counts_more_and_fewer_with_ties_in_neither(self):
        alone = ac.report(self.RESULTS, {"grassPull": 0.2}, [500, 1000])["alone"]
        self.assertEqual((alone["more"], alone["fewer"]), (1, 2))

    def test_genes_count_seeds_above_the_original_on_the_last_day(self):
        genes = ac.report(self.RESULTS, {"grassPull": 0.2}, [500, 1000])["genes"]["grassPull"]
        self.assertEqual((genes["original"], genes["up"], genes["of"]), (0.2, 3, 4))
        self.assertEqual(genes["medians"], {500: 0.5, 1000: 0.9})


class TextTest(unittest.TestCase):
    def test_names_each_section_without_a_verdict(self):
        text = ac.format_text(ac.report(ReportTest.RESULTS, {"grassPull": 0.2}, [500, 1000]), 12.0, per_seed=False)
        for heading in ("both groups in one world", "each group in a world of its own", "evolved genes"):
            self.assertIn(heading, text)
        self.assertNotIn("pass", text.lower())
        self.assertNotIn("fail", text.lower())


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
        self.assertEqual(lines[0], '{"species": "rabbit", "genes": {"grassPull": 0.2, "whim": 2.0}}')


if __name__ == "__main__":
    unittest.main()
