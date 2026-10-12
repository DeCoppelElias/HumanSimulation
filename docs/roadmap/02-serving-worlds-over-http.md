# 2. Serving worlds over HTTP

Status: done.

A Javalin adapter serves an HTTP API, per
[0020](../decisions/0020-the-interface-is-a-web-page.md), listening on `127.0.0.1`
unless `--host` says otherwise. Worlds are held by an unguessable id, listed,
capped at 32 by `--max-worlds`, and closed by deleting them.

| Request | Does |
|---|---|
| `POST /worlds` | Creates an empty, paused world, 20 by 20 with a seed from the clock unless `width`, `height` or `seed` say otherwise, at most 25 a side. |
| `GET /worlds` | Lists each world's id, size, seed, day, population and whether it plays. |
| `GET /worlds/{id}`, `DELETE /worlds/{id}` | The current snapshot, or closing the world. |
| `POST /worlds/{id}/commands` | One command, such as `{"type":"spawn","species":"rabbit","count":3}`. On a paused world it applies at once and returns the snapshot, on a playing one it returns `202` and lands the next day. A `reset` without a seed draws one from the clock. |
| `POST /worlds/{id}/step` | Advances a paused world `days` days, 1 by default and at most 10,000, and returns the last day's report. One step at a time per world. |
| `POST /worlds/{id}/play`, `/pause`, `PUT /worlds/{id}/rate` | The world's timer, at 0.5 to 10 days a second, 2 by default. |
| `GET /worlds/{id}/census?from=N`, `DELETE /worlds/{id}/census` | The census history, or the statistics reset. |
| `GET /worlds/{id}/log` | Size, seed and commands, in the shape the commands route takes. |
| `GET /worlds/{id}/events` | Server-Sent Events. |

Errors are `{"error":"..."}`: `400` for input the domain or the limits reject,
`404` for an unknown world, `409` for stepping a playing world, a second step
in flight, or creating past the cap.

Each world has one executor thread that owns it and advances it at the playback
rate. The stream is fire and forget: each viewer holds the latest day's
snapshot with its census row, a slow viewer skips days, and a reader that sees
a gap fetches the missing rows from the history. A statistics reset sends
`censusReset`, and a world reset clears the history too, since its day numbers
start again. A `: keepalive` comment goes out every 15 seconds.

`serve` prints `{"event":"ready","url":...,"port":...}` on stdout once it
accepts requests, takes `--port 0`, and logs to stderr, per
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md). `serve
--no-browser` quits after 10 minutes without a request or a stream. `serve`
never quits, does not open a browser until entry 3, and pauses playing worlds
after 24 hours without a request or a stream. `--idle-minutes` adjusts either.
JUnit covers the API against a running server.

Regression rules that land here: resetting the statistics does not break the
population history.

On the screen: an agent starts `serve --no-browser --port 0`, reads the port
from the ready line, and with `curl` creates a world, spawns rabbits, steps it
500 days and reads the snapshot, the census and the log back.

Depends on entry 1. See
[0007](../decisions/0007-state-leaves-as-a-snapshot-commands-go-in.md),
[0010](../decisions/0010-runs-replay-exactly-from-a-seed.md),
[0020](../decisions/0020-the-interface-is-a-web-page.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).
