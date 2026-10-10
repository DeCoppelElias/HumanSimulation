# Bounded census history

Status: idea.

## What it does

Limits how much census history the server keeps per world. Today every day's
row is kept until a statistics reset or a world reset, so a world playing at 10
days a second gains about 864,000 rows a day.

## Why it is interesting

A person's worlds are never deleted, and a world left playing for days is a
case the server is meant to support, so the history is the one thing that grows
without bound. Long before memory runs out, `GET /census` without `from` gets
slow to send.

## What it would touch

The web adapter's history in `WorldHost`, and how the page's chart and gap
filling ask for rows. The domain is untouched, since the history belongs to the
adapter, per [0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md).

## Open questions

Whether old rows are dropped, or thinned to every tenth or hundredth day so a
long chart keeps its shape.

What a reader asking for a day that was dropped gets back.
