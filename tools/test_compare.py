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


class SpeciesTest(unittest.TestCase):
    def test_every_species_with_genes_on_day_one(self):
        self.assertEqual(compare.species_in([line(1, 4, grass=9)]), ["rabbit"])


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


class TextTest(unittest.TestCase):
    def test_states_the_count_without_a_verdict(self):
        results = {("control", 1): run(300), ("mutating", 1): run(400)}
        text = compare.format_text(
            {"rabbit": compare.report(results, ["control", "mutating"], [1], "rabbit")}, per_seed=False
        )
        self.assertIn("mutating later than control: 1 of 1, ties 0, earlier 0", text)
        self.assertNotIn("pass", text.lower())


if __name__ == "__main__":
    unittest.main()
