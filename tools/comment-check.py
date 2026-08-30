#!/usr/bin/env python3
"""Flags comments that describe history, run long, or hold commented-out code.

Usage:
    python tools/comment-check.py <file>...
    python tools/comment-check.py --staged

Only mechanical rules live here. Whether a comment earns its place is a
judgement call the reader makes; this catches the three failures a regex can
see. Exit code is 1 while findings remain.
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path

MAX_BLOCK_LINES = 4

# Comments should describe what the code does now, not how it got here.
HISTORY = re.compile(
    r"\b("
    r"used to|previously|formerly|no longer|we now|now uses|now returns|"
    r"changed (?:from|to)|renamed (?:from|to)|moved (?:from|to)|"
    r"instead of the old|as before|verified:|"
    r"todo|fixme|xxx|hack"
    r")\b",
    re.IGNORECASE,
)

# A comment line that is really a line of code.
CODE_LIKE = re.compile(r"^\s*(?:[\w.\[\]<>]+\s*[=(].*[;)]|[{}]|</?\w+>)\s*$")

MARKERS = {
    ".java": [("//", None), ("/*", "*/")],
    ".xml": [(None, None), ("<!--", "-->")],
    ".yml": [("#", None)],
    ".yaml": [("#", None)],
    ".properties": [("#", None)],
    ".editorconfig": [("#", None)],
    ".gitattributes": [("#", None)],
    ".sh": [("#", None)],
    ".py": [("#", None)],
}


def comment_lines(path: Path):
    """Yield (lineno, text) for each comment line, best effort per file type."""
    suffix = path.suffix or path.name
    if suffix not in MARKERS:
        return

    text = path.read_text(encoding="utf-8", errors="replace").splitlines()
    in_block = False
    for n, raw in enumerate(text, 1):
        stripped = raw.strip()

        if in_block:
            yield n, strip_markers(stripped)
            if "*/" in stripped or "-->" in stripped:
                in_block = False
            continue

        if stripped.startswith(("/*", "<!--")):
            in_block = not (stripped.endswith("*/") or stripped.endswith("-->"))
            yield n, strip_markers(stripped)
        elif stripped.startswith("//") or (suffix != ".java" and stripped.startswith("#")):
            yield n, strip_markers(stripped)


def strip_markers(line: str) -> str:
    for marker in ("<!--", "-->", "/**", "/*", "*/", "//", "#"):
        line = line.replace(marker, " ")
    return line.strip().lstrip("* ").strip()


def check(path: Path):
    findings = []
    block_start = None
    block_len = 0
    previous_line = None

    for lineno, body in comment_lines(path):
        if HISTORY.search(body):
            word = HISTORY.search(body).group(1)
            findings.append((lineno, f"describes history or intent, not current state ({word!r})"))
        if body and CODE_LIKE.match(body):
            findings.append((lineno, "looks like commented-out code"))

        if previous_line is not None and lineno == previous_line + 1:
            block_len += 1
        else:
            if block_len > MAX_BLOCK_LINES:
                findings.append((block_start, f"comment block is {block_len} lines, keep it to {MAX_BLOCK_LINES}"))
            block_start, block_len = lineno, 1
        previous_line = lineno

    if block_len > MAX_BLOCK_LINES:
        findings.append((block_start, f"comment block is {block_len} lines, keep it to {MAX_BLOCK_LINES}"))

    return sorted(findings)


def staged_files():
    out = subprocess.run(
        ["git", "diff", "--cached", "--name-only", "--diff-filter=ACM"],
        capture_output=True,
        text=True,
        check=True,
    )
    return [Path(p) for p in out.stdout.split() if Path(p).is_file()]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("files", nargs="*", type=Path)
    parser.add_argument("--staged", action="store_true", help="check the staged files")
    args = parser.parse_args()

    targets = staged_files() if args.staged else args.files
    if not targets:
        print("clean")
        return 0

    total = 0
    for path in targets:
        for lineno, message in check(path):
            print(f"{path}:{lineno}: {message}")
            total += 1

    if total:
        print(f"\n{total} comment finding(s). Comments describe the current state, earn their place, and stay short.")
        return 1

    print("clean")
    return 0


if __name__ == "__main__":
    sys.exit(main())
