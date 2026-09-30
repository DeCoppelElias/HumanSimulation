"""Tests for docs-check.py. Run with: python tools/test_docs_check.py"""

import importlib.util
import tempfile
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location(
    "docs_check", Path(__file__).with_name("docs-check.py")
)
docs_check = importlib.util.module_from_spec(spec)
spec.loader.exec_module(docs_check)

DECISION = """# 0001. First topic

## Status

Accepted, 2026-09-03.

## Context

Text.
"""

IDEA = """# Some idea

Status: idea.

## What it does

## Why it is interesting

## What it would touch

## Open questions
"""


class DocsCheckTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        self.write("DESIGN.md", "# Design\n\nSee [log](docs/decisions/README.md).\n")
        self.write("docs/decisions/0001-first-topic.md", DECISION)
        self.write(
            "docs/decisions/README.md",
            "# Decision log\n\n- [0001](0001-first-topic.md) First topic.\n",
        )
        self.write("docs/ideas/some-idea.md", IDEA)
        self.write("docs/ideas/README.md", "# Ideas\n\n- [Some idea](some-idea.md)\n")
        self.write("docs/roadmap.md", "# Roadmap\n\n## 1. First\n\nStatus: not started.\n")

    def tearDown(self):
        self.tmp.cleanup()

    def write(self, rel, text):
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def findings(self):
        return docs_check.check(self.root)

    def assertFinding(self, fragment):
        found = self.findings()
        self.assertTrue(any(fragment in f for f in found), f"{fragment!r} not in {found}")

    def test_consistent_tree_has_no_findings(self):
        self.assertEqual([], self.findings())

    def test_broken_relative_link(self):
        self.write("DESIGN.md", "See [gone](docs/decisions/0099-gone.md).\n")
        self.assertFinding("broken link docs/decisions/0099-gone.md")

    def test_links_inside_code_are_ignored(self):
        self.write("DESIGN.md", "`[x](nowhere.md)`\n\n```\n[y](nowhere.md)\n```\n")
        self.assertEqual([], self.findings())

    def test_backticked_docs_path_must_exist(self):
        self.write("DESIGN.md", "Kept in `docs/ideas/missing-idea.md`.\n")
        self.assertFinding("missing path docs/ideas/missing-idea.md")

    def test_decision_not_in_index(self):
        self.write("docs/decisions/0002-second-topic.md", DECISION.replace("0001", "0002"))
        self.assertFinding("0002-second-topic.md is not in docs/decisions/README.md")

    def test_decision_heading_number_mismatch(self):
        self.write("docs/decisions/0001-first-topic.md", DECISION.replace("# 0001.", "# 0003."))
        self.assertFinding("heading number")

    def test_decision_without_status(self):
        self.write("docs/decisions/0001-first-topic.md", "# 0001. First topic\n\nText.\n")
        self.assertFinding("no status")

    def test_duplicate_decision_number(self):
        self.write("docs/decisions/0001-other-topic.md", DECISION)
        self.assertFinding("number 0001 is used twice")

    def test_idea_not_in_index(self):
        self.write("docs/ideas/other-idea.md", IDEA)
        self.assertFinding("other-idea.md is not in docs/ideas/README.md")

    def test_idea_with_bad_status(self):
        self.write("docs/ideas/some-idea.md", IDEA.replace("Status: idea.", "Status: maybe."))
        self.assertFinding("status")

    def test_built_idea_must_link_a_decision(self):
        self.write("docs/ideas/some-idea.md", IDEA.replace("Status: idea.", "Status: built."))
        self.assertFinding("built but links no decision")

    def test_idea_missing_section(self):
        self.write("docs/ideas/some-idea.md", IDEA.replace("## Open questions\n", ""))
        self.assertFinding("missing section Open questions")

    def test_roadmap_entry_without_status(self):
        self.write("docs/roadmap.md", "# Roadmap\n\n## 1. First\n\nText.\n")
        self.assertFinding("1. First has no status")

    def test_roadmap_entry_with_bad_status(self):
        self.write("docs/roadmap.md", "# Roadmap\n\n## 1. First\n\nStatus: half done.\n")
        self.assertFinding("1. First has status")

    def test_superpowers_folder_is_skipped(self):
        self.write("docs/superpowers/plans/p.md", "[x](nowhere.md)\n")
        self.assertEqual([], self.findings())


if __name__ == "__main__":
    unittest.main()
