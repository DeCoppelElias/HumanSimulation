#!/usr/bin/env python3
"""Checks that the committed docs agree with each other where a regex can tell.

Usage:
    python tools/docs-check.py

Covers links and backticked docs/ paths that resolve, the decision and idea
indexes, decision numbering and status, idea status and sections, and a status
on every roadmap entry. Whether DESIGN.md says the same thing as a decision is
a judgement call and belongs to the auditing-docs skill. Exit code is 1 while
findings remain.
"""

import re
import sys
from pathlib import Path

IDEA_STATUSES = {"idea", "planned", "built"}
ROADMAP_STATUSES = {"not started", "in progress", "done"}
IDEA_SECTIONS = ["What it does", "Why it is interesting", "What it would touch", "Open questions"]

FENCE = re.compile(r"^```.*?^```", re.MULTILINE | re.DOTALL)
INLINE_CODE = re.compile(r"`[^`\n]*`")
LINK = re.compile(r"\[[^\]]*\]\(([^)\s]+)\)")
DOCS_PATH = re.compile(r"`(docs/[\w./-]+\.md)`")
DECISION_FILE = re.compile(r"^(\d{4})-[\w-]+\.md$")
STATUS_LINE = re.compile(r"^Status: (.+?)\.?$", re.MULTILINE)


def markdown_files(root: Path):
    for path in sorted(root.glob("*.md")):
        yield path
    for base in ("docs", ".claude/skills"):
        for path in sorted((root / base).rglob("*.md")):
            if "superpowers" not in path.relative_to(root).parts:
                yield path


def blank_out(pattern, text):
    """Replace matches with spaces, keeping newlines so line numbers survive."""
    return pattern.sub(lambda m: re.sub(r"[^\n]", " ", m.group(0)), text)


def line_of(text, index):
    return text.count("\n", 0, index) + 1


def check_links(root, path, text, findings):
    rel = path.relative_to(root).as_posix()
    prose = blank_out(INLINE_CODE, blank_out(FENCE, text))
    for m in LINK.finditer(prose):
        target = m.group(1).split("#")[0]
        if not target or re.match(r"^[a-z]+:", target):
            continue
        if not (path.parent / target).exists():
            findings.append(f"{rel}:{line_of(text, m.start())}: broken link {target}")

    unfenced = blank_out(FENCE, text)
    for m in DOCS_PATH.finditer(unfenced):
        if not (root / m.group(1)).exists():
            findings.append(f"{rel}:{line_of(text, m.start())}: missing path {m.group(1)}")


def check_decisions(root, findings):
    folder = root / "docs/decisions"
    index = (folder / "README.md").read_text(encoding="utf-8")
    seen = {}
    for path in sorted(folder.glob("*.md")):
        m = DECISION_FILE.match(path.name)
        if not m:
            continue
        number = m.group(1)
        rel = f"docs/decisions/{path.name}"
        if number in seen:
            findings.append(f"{rel}: number {number} is used twice, also by {seen[number]}")
        seen[number] = path.name

        text = path.read_text(encoding="utf-8")
        heading = re.match(r"# (\d{4})\. ", text)
        if not heading or heading.group(1) != number:
            findings.append(f"{rel}:1: heading number does not match the file name")
        status = re.search(r"^## Status\s*\n\s*\n(.+)$", text, re.MULTILINE)
        if not status or not re.match(r"(Accepted|Proposed), \d{4}-\d{2}-\d{2}", status.group(1)):
            findings.append(f"{rel}: no status line reading Accepted or Proposed with a date")
        if f"({path.name})" not in index:
            findings.append(f"{rel} is not in docs/decisions/README.md")


def check_ideas(root, findings):
    folder = root / "docs/ideas"
    index = (folder / "README.md").read_text(encoding="utf-8")
    for path in sorted(folder.glob("*.md")):
        if path.name == "README.md":
            continue
        rel = f"docs/ideas/{path.name}"
        text = path.read_text(encoding="utf-8")
        if f"({path.name})" not in index:
            findings.append(f"{rel} is not in docs/ideas/README.md")

        status = STATUS_LINE.search(text)
        word = status.group(1).split()[0].rstrip(".,") if status else None
        if word not in IDEA_STATUSES:
            findings.append(f"{rel}: status must start with one of {sorted(IDEA_STATUSES)}")
        elif word == "built" and "../decisions/" not in status.group(1):
            findings.append(f"{rel}: built but links no decision in its status line")

        for section in IDEA_SECTIONS:
            if not re.search(rf"^## {section}\s*$", text, re.MULTILINE):
                findings.append(f"{rel}: missing section {section}")


def check_roadmap(root, findings):
    text = (root / "docs/roadmap.md").read_text(encoding="utf-8")
    entries = re.split(r"^## ", text, flags=re.MULTILINE)[1:]
    for entry in entries:
        title = entry.splitlines()[0].strip()
        status = STATUS_LINE.search(entry)
        if not status:
            findings.append(f"docs/roadmap.md: {title} has no status line")
        elif status.group(1) not in ROADMAP_STATUSES:
            findings.append(
                f"docs/roadmap.md: {title} has status {status.group(1)!r},"
                f" expected one of {sorted(ROADMAP_STATUSES)}"
            )


def check(root: Path):
    findings = []
    for path in markdown_files(root):
        check_links(root, path, path.read_text(encoding="utf-8"), findings)
    check_decisions(root, findings)
    check_ideas(root, findings)
    check_roadmap(root, findings)
    return findings


def main():
    root = Path(__file__).resolve().parent.parent
    findings = check(root)
    for finding in findings:
        print(finding)
    return 1 if findings else 0


if __name__ == "__main__":
    sys.exit(main())
