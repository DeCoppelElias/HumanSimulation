# World size on the page

Status: idea.

## What it does

The page's create screen asks for a width and a height beside the seed, and
sends them with the new world. `POST /worlds` already accepts both, and `run`
has `--width` and `--height`, so only the page is missing them. Today a world
made on the page is always 20 by 20.

## Why it is interesting

Size changes how a run plays. A small world crowds rabbits onto the same food,
and a large one lets them spread out, so trying a run at a few sizes is one of
the first experiments a person watching would want.

## What it would touch

The create screen and the request it sends. Size stays a constructor input
rather than a setting, per
[0017](../decisions/0017-every-number-is-a-setting-or-a-gene.md), so the
settings panel is not involved, and a reset keeps the size.

## Open questions

The largest size the page allows before a world stops being readable on
screen, and whether a reset should be able to change the size.
