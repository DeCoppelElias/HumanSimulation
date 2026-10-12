# 11. Hosting

Status: not started.

The app as a link anyone can open. Each visitor's worlds belong to their
session, abandoned worlds are cleaned up, and a cap on worlds across sessions
keeps a spike in traffic from exhausting the machine. A container image and a
deploy workflow put it on Fly.io.

API tests cover that a second session can neither see nor delete the first's
worlds, that the cap refuses a new world once reached, and that an abandoned
world is removed after its timeout.

Details to settle in this entry's grill: how many worlds a session may hold, the
cap across sessions and the idle timeout, sized against what a world costs in
memory and threads; what the page shows a visitor when a cap is reached;
limiting the rate of requests per session, since a stranger can step and play
worlds as fast as the server allows; and how cleaning up abandoned worlds fits
the rule from entry 2 that a local server never deletes a person's world.

On the screen: the link opens the page with a fresh world, and two browsers
opening it each get their own.

Depends on entry 6, and comes after entry 10 so the first link strangers open
shows the finished look. See
[0020](../decisions/0020-the-interface-is-a-web-page.md),
[0021](../decisions/0021-agents-drive-worlds-over-http-and-run.md).
