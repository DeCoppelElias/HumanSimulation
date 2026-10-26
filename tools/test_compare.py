"""Tests for compare.py. Run with: python tools/test_compare.py"""

import importlib.util
import json
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location("compare", Path(__file__).with_name("compare.py"))
compare = importlib.util.module_from_spec(spec)
spec.loader.exec_module(compare)


def line(day, rabbits, speed=0.5, grass=0):
    genes = {"rabbit": {"speed": speed}} if rabbits else {}
    return json.dumps(
        {"seed": 1, "day": day, "population": {"grass": grass, "rabbit": rabbits}, "geneMeans": genes}
    )


class SeedsTest(unittest.TestCase):
    def test_a_range_is_inclusive(self):
        self.assertEqual(compare.parse_seeds("3-5"), [3, 4, 5])

    def test_one_seed(self):
        self.assertEqual(compare.parse_seeds("7"), [7])

    def test_a_backwards_range_is_refused(self):
        with self.assertRaises(ValueError):
            compare.parse_seeds("5-3")


def two_species(day, rabbits, foxes):
    genes = {}
    if rabbits:
        genes["rabbit"] = {"speed": 0.5}
    if foxes:
        genes["fox"] = {"speed": 0.7}
    return json.dumps(
        {"seed": 1, "day": day, "population": {"fox": foxes, "grass": 9, "rabbit": rabbits}, "geneMeans": genes}
    )


class SpeciesTest(unittest.TestCase):
    def test_every_species_with_genes_on_day_one_of_any_run(self):
        runs = [[two_species(1, 4, 0)], [two_species(1, 0, 2)]]
        self.assertEqual(compare.species_in(runs), ["fox", "rabbit"])

    def test_a_species_a_run_never_had_is_absent_rather_than_dead(self):
        summary = compare.summarise([two_species(1, 4, 0), two_species(2, 4, 0)], "fox", 5)
        self.assertEqual(summary["diedOn"], 0)
        self.assertEqual(summary["peak"], 0)


class SummaryTest(unittest.TestCase):
    def test_dies_on_the_first_day_with_none(self):
        lines = [line(1, 8), line(2, 3), line(3, 0), line(4, 0)]
        self.assertEqual(compare.summarise(lines, "rabbit", 5)["diedOn"], 3)

    def test_a_survivor_has_no_death_day(self):
        self.assertIsNone(compare.summarise([line(1, 8), line(2, 8)], "rabbit", 5)["diedOn"])

    def test_population_counts_only_days_alive(self):
        summary = compare.summarise([line(1, 6), line(2, 2), line(3, 0)], "rabbit", 5)
        self.assertEqual(summary["meanPopulation"], 4)
        self.assertEqual(summary["peak"], 6)


class MeasuredOnTest(unittest.TestCase):
    def test_genes_come_from_the_last_day_at_or_above_the_floor(self):
        lines = [line(1, 9, 0.5), line(2, 5, 0.3), line(3, 2, 0.9), line(4, 0)]
        summary = compare.summarise(lines, "rabbit", 5)
        self.assertEqual(summary["measuredOn"], 2)
        self.assertEqual(summary["genes"], {"speed": 0.3})
        self.assertEqual(summary["start"], {"speed": 0.5})

    def test_never_reaching_the_floor_measures_nothing(self):
        summary = compare.summarise([line(1, 3), line(2, 0)], "rabbit", 5)
        self.assertIsNone(summary["measuredOn"])
        self.assertEqual(summary["genes"], {})


def run(died_on, speed=0.5, start=0.5):
    return {
        "diedOn": died_on,
        "meanPopulation": 10,
        "peak": 20,
        "start": {"speed": start},
        "measuredOn": 100,
        "genes": {"speed": speed},
    }


class OutlastsTest(unittest.TestCase):
    def test_later_death_and_survival_beat_earlier_death_and_two_survivors_tie(self):
        results = {
            ("control", 1): run(300),
            ("mutating", 1): run(400),
            ("control", 2): run(300),
            ("mutating", 2): run(None),
            ("control", 3): run(None),
            ("mutating", 3): run(None),
            ("control", 4): run(500),
            ("mutating", 4): run(200),
        }
        report = compare.report(results, ["control", "mutating"], [1, 2, 3, 4], "rabbit")
        self.assertEqual(
            report["outlasts"],
            [{"variant": "mutating", "reference": "control", "later": 2, "ties": 1, "earlier": 1}],
        )


class GeneReportTest(unittest.TestCase):
    def test_counts_seeds_above_their_own_start(self):
        results = {
            ("mutating", 1): run(None, speed=0.3, start=0.5),
            ("mutating", 2): run(None, speed=0.6, start=0.5),
            ("mutating", 3): run(None, speed=0.45, start=0.4),
        }
        genes = compare.report(results, ["mutating"], [1, 2, 3], "rabbit")["genes"]["speed"]["mutating"]
        self.assertEqual(genes["above"], 2)
        self.assertEqual(genes["of"], 3)
        self.assertEqual((genes["min"], genes["median"], genes["max"]), (0.3, 0.45, 0.6))


class RoundingTest(unittest.TestCase):
    def test_a_mean_that_only_differs_by_rounding_is_not_above(self):
        results = {("control", 1): run(None, speed=0.20000000000000004, start=0.2)}
        genes = compare.report(results, ["control"], [1], "rabbit")["genes"]["speed"]["control"]
        self.assertEqual(genes["above"], 0)


class TextTest(unittest.TestCase):
    def test_states_the_count_without_a_verdict(self):
        results = {("control", 1): run(300), ("mutating", 1): run(400)}
        text = compare.format_text(
            {"rabbit": compare.report(results, ["control", "mutating"], [1], "rabbit")}, per_seed=False
        )
        self.assertIn("mutating later than control: 1 of 1, ties 0, earlier 0", text)
        self.assertNotIn("pass", text.lower())

    def test_counts_survivors_per_variant_and_shows_an_absent_run(self):
        results = {
            ("defaults", 1): run(None),
            ("defaults", 2): run(300),
            ("defaults", 3): run(None),
            ("defaults", 4): run(0),
        }
        text = compare.format_text(
            {"rabbit": compare.report(results, ["defaults"], [1, 2, 3, 4], "rabbit")}, False
        )
        self.assertIn("defaults alive at the end: 2 of 4", text)
        self.assertRegex(text, r"\n4 +absent\n")

    def test_per_seed_names_each_gene_and_a_run_that_never_reached_the_floor(self):
        thin = dict(run(40), measuredOn=None, genes={})
        results = {("mutating", 1): run(None, speed=0.95), ("mutating", 2): thin}
        text = compare.format_text(
            {"rabbit": compare.report(results, ["mutating"], [1, 2], "rabbit", floor=7)}, True
        )
        self.assertIn("mutating 1 day 100: speed=0.95", text)
        self.assertIn("mutating 2 never had 7 or more", text)


class RunFailureTest(unittest.TestCase):
    def test_a_failed_run_names_its_variant_and_seed(self):
        with self.assertRaises(compare.RunFailed) as caught:
            compare.check_output("control", 3, 2, "", "setting stride is 9, outside 1 to 5")
        self.assertIn("control", str(caught.exception))
        self.assertIn("seed 3", str(caught.exception))
        self.assertIn("outside", str(caught.exception))

    def test_the_reason_is_the_first_line_without_colour(self):
        with self.assertRaises(compare.RunFailed) as caught:
            compare.check_output("bad", 1, 2, "", "\x1b[31m\x1b[1mno setting wings\x1b[21m\x1b[0m\nUsage: run")
        self.assertEqual(str(caught.exception), "bad, seed 1: no setting wings")

    def test_a_run_that_crashes_partway_is_a_failure_whatever_it_printed(self):
        with self.assertRaises(compare.RunFailed):
            compare.check_output("control", 3, 1, line(1, 8) + "\n" + line(2, 8), "OutOfMemoryError")

    def test_a_run_that_prints_nothing_is_a_failure(self):
        with self.assertRaises(compare.RunFailed):
            compare.check_output("control", 3, 0, "", "")


class PresetTest(unittest.TestCase):
    PRESET = {
        "seeds": "1-10",
        "days": 1000,
        "flags": ["--set-at", "100:world.grassPerDrop=6"],
        "variants": {"control": ["--set", "world.mutation=0"], "mutating": []},
    }

    def test_flags_override_the_preset(self):
        chosen = compare.plan(self.PRESET, compare.parse_args(["--seeds", "11-12", "--days", "50"]))
        self.assertEqual(chosen["seeds"], [11, 12])
        self.assertEqual(chosen["days"], 50)

    def test_each_variant_gets_the_shared_flags_and_only_its_own(self):
        chosen = compare.plan(self.PRESET, compare.parse_args([]))
        self.assertEqual(
            chosen["variants"],
            {
                "control": ["--set-at", "100:world.grassPerDrop=6", "--set", "world.mutation=0"],
                "mutating": ["--set-at", "100:world.grassPerDrop=6"],
            },
        )

    def test_variants_on_the_command_line_replace_the_presets(self):
        args = compare.parse_args(["--variant", "cheap=--set rabbit.speedCharge=0.05", "--variant", "base="])
        chosen = compare.plan(self.PRESET, args)
        self.assertEqual(list(chosen["variants"]), ["cheap", "base"])
        self.assertEqual(chosen["variants"]["base"], ["--set-at", "100:world.grassPerDrop=6"])

    def test_a_run_command_names_the_seed_days_and_flags(self):
        self.assertEqual(
            compare.command("app.jar", 3, 20, ["--set", "world.mutation=0"]),
            ["java", "-jar", "app.jar", "run", "--seed", "3", "--days", "20", "--set", "world.mutation=0"],
        )


if __name__ == "__main__":
    unittest.main()
