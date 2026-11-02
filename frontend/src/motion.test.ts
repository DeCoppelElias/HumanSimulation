import { describe, expect, test } from "vitest";
import {
    blinking,
    frameAt,
    moveMs,
    pose,
    pounced,
    settledMs,
    strideOf,
    timing,
    transition,
    turned,
    type Moment,
} from "./motion";
import type { Placed } from "./tiles";

const creature = (id: number, spriteKey: string, x: number, ox = 0): Placed => ({
    id,
    spriteKey,
    creature: true,
    x,
    y: 0,
    ox,
    oy: 0,
});
const rabbit = (id: number, x: number, ox = 0) => creature(id, "rabbit", x, ox);

describe("timing", () => {
    const at = (daysPerSecond: number) => timing(1, { playing: true, daysPerSecond });

    test("a move takes 260 ms and 160 ms a tile", () => {
        expect(moveMs(at(1), 1)).toBe(420);
        expect(moveMs(at(1), 3)).toBe(740);
    });

    test("is cut to 90% of the day", () => {
        expect(moveMs(at(1), 5)).toBe(900);
        expect(moveMs(at(2), 2)).toBe(450);
    });

    test("glides when the cut leaves under 150 ms", () => {
        expect(at(10).glide).toBe(true);
        expect(moveMs(at(10), 3)).toBe(90);
        expect(at(6).glide).toBe(false);
        expect(moveMs(at(6), 1)).toBeCloseTo(150, 6);
    });

    test("a paused step plays the move at full length", () => {
        const step = timing(1, { playing: false, daysPerSecond: 10 });
        expect(step.glide).toBe(false);
        expect(moveMs(step, 5)).toBe(1060);
    });

    test("only a one-day step moves, and fades always run", () => {
        expect(timing(5, { playing: true, daysPerSecond: 2 }).moves).toBe(false);
        expect(timing(0, { playing: false, daysPerSecond: 2 }).moves).toBe(false);
        expect(timing(1, { playing: false, daysPerSecond: 2 }).fadeMs).toBe(300);
        expect(timing(5, { playing: true, daysPerSecond: 10 }).fadeMs).toBe(90);
    });
});

describe("stride", () => {
    test("a rabbit bounds any move", () => {
        expect(strideOf("rabbit", 1, false, false)).toBe("bound");
        expect(strideOf("rabbit", 4, false, false)).toBe("bound");
    });

    test("a wolf walks one tile and leaps a longer move or a kill", () => {
        expect(strideOf("wolf", 1, false, false)).toBe("walk");
        expect(strideOf("wolf", 2, false, false)).toBe("leap");
        expect(strideOf("wolf", 1, true, false)).toBe("leap");
    });

    test("everything glides when the timing says so, and an unknown species always does", () => {
        expect(strideOf("rabbit", 3, false, true)).toBe("glide");
        expect(strideOf("wolf", 1, true, true)).toBe("glide");
        expect(strideOf("deer", 2, false, false)).toBe("glide");
    });
});

describe("a pounce", () => {
    const from = new Map([
        [1, creature(1, "wolf", 0)],
        [2, rabbit(2, 1)],
    ]);

    test("is a move onto a tile where a creature of another species stood and is gone", () => {
        expect(pounced(from, new Map([[1, creature(1, "wolf", 1)]]), 1)).toBe(true);
    });

    test("is not a move onto a creature that is still there, or that moved away", () => {
        const stayed = new Map([
            [1, creature(1, "wolf", 1)],
            [2, rabbit(2, 1)],
        ]);
        const fled = new Map([
            [1, creature(1, "wolf", 1)],
            [2, rabbit(2, 3)],
        ]);
        expect(pounced(from, stayed, 1)).toBe(false);
        expect(pounced(from, fled, 1)).toBe(false);
    });

    test("is not a move onto a tile that a creature of its own species left", () => {
        const pack = new Map([
            [1, creature(1, "wolf", 0)],
            [3, creature(3, "wolf", 1)],
        ]);
        expect(pounced(pack, new Map([[1, creature(1, "wolf", 1)]]), 1)).toBe(false);
    });

    test("is not a move onto a tile where grass was eaten", () => {
        const meadow = new Map([
            [1, rabbit(1, 0)],
            [4, { ...rabbit(4, 1), spriteKey: "grass", creature: false }],
        ]);
        expect(pounced(meadow, new Map([[1, rabbit(1, 1)]]), 1)).toBe(false);
    });
});

describe("poses", () => {
    test("a bound crouches, flies in the tucked frame, and crouches again", () => {
        expect(pose("bound", 1, 0.1)).toEqual({ frame: "crouch", along: 0, lift: 0 });
        expect(pose("bound", 1, 0.5)).toEqual({ frame: "air", along: 0.5, lift: 7 });
        expect(pose("bound", 1, 0.9)).toEqual({ frame: "crouch", along: 1, lift: 0 });
        expect(pose("bound", 1, 1)).toEqual({ frame: "stand", along: 1, lift: 0 });
    });

    test("a longer bound arcs higher", () => {
        expect(pose("bound", 3, 0.5).lift).toBe(12);
    });

    test("a walk alternates stride and stand twice a tile and never lifts", () => {
        expect(pose("walk", 1, 0.1)).toEqual({ frame: "stride", along: 0.1, lift: 0 });
        expect(pose("walk", 1, 0.6)).toEqual({ frame: "stand", along: 0.6, lift: 0 });
    });

    test("a leap gathers, takes off, flies, lands and stands", () => {
        expect(pose("leap", 2, 0.1)).toEqual({ frame: "gather", along: 0, lift: 0 });
        expect(pose("leap", 2, 0.2).frame).toBe("takeoff");
        expect(pose("leap", 2, 0.425)).toEqual({ frame: "flight", along: 0.5, lift: 6.5 });
        expect(pose("leap", 2, 0.8)).toEqual({ frame: "land", along: 1, lift: 0 });
        expect(pose("leap", 2, 0.9).frame).toBe("stand");
    });

    test("a glide keeps the standing pose", () => {
        expect(pose("glide", 3, 0.5)).toEqual({ frame: "stand", along: 0.5, lift: 0 });
    });
});

describe("turning", () => {
    test("faces the side it moves toward and keeps it moving straight up or down", () => {
        expect(turned(undefined, -2)).toBe(true);
        expect(turned(true, 3)).toBe(false);
        expect(turned(true, 0)).toBe(true);
        expect(turned(undefined, 0)).toBe(false);
    });
});

describe("blink", () => {
    test("comes two to four times in ten seconds and lasts 150 ms", () => {
        for (const id of [1, 2, 17, 400]) {
            let count = 0;
            let longest = 0;
            let run = 0;
            let was = false;
            for (let now = 0; now < 10_000; now += 10) {
                const is = blinking(id, now);
                if (is && !was) count++;
                run = is ? run + 10 : 0;
                longest = Math.max(longest, run);
                was = is;
            }
            expect(count).toBeGreaterThanOrEqual(2);
            expect(count).toBeLessThanOrEqual(4);
            expect(longest).toBe(150);
        }
    });

    test("does not keep two creatures in step", () => {
        const at = (id: number) => Array.from({ length: 1000 }, (_, i) => blinking(id, i * 10));
        expect(at(1)).not.toEqual(at(2));
    });
});

describe("a frame", () => {
    const snap = (day: number, ...placed: Placed[]): Moment => ({
        day,
        placed: new Map(placed.map((p) => [p.id, p])),
        drawn: new Set(placed.map((p) => p.id)),
    });
    const paused = { playing: false, daysPerSecond: 1 };
    const fast = { playing: true, daysPerSecond: 10 };
    // A moment when rabbit 1 is not blinking, so poses read plainly.
    const open = Array.from({ length: 500 }, (_, i) => i * 10).find((now) => !blinking(1, now))!;

    test("a stepped rabbit bounds a tile over 420 ms", () => {
        const move = transition(snap(1, rabbit(1, 0)), snap(2, rabbit(1, 1)), paused, new Map());
        expect(frameAt(move, 0, open)[0]).toMatchObject({ frame: "crouch", x: 0, lift: 0 });
        expect(frameAt(move, 210, open)[0]).toMatchObject({ frame: "air", x: 0.5, lift: 7 });
        expect(frameAt(move, 420, open)[0]).toMatchObject({ frame: "stand", x: 1 });
    });

    test("at 10 days a second it glides in the standing pose", () => {
        const move = transition(snap(1, rabbit(1, 0)), snap(2, rabbit(1, 2)), fast, new Map());
        expect(frameAt(move, 45, open)[0]).toMatchObject({ frame: "stand", x: 1, lift: 0 });
    });

    test("a wolf onto a rabbit that is gone leaps a single tile", () => {
        const move = transition(
            snap(1, creature(1, "wolf", 0), rabbit(2, 1)),
            snap(2, creature(1, "wolf", 1)),
            paused,
            new Map(),
        );
        expect(frameAt(move, 30, open)[0]).toMatchObject({ id: 1, frame: "gather" });
    });

    test("across a jump it is at its new tile at once and a new id still fades in", () => {
        const jump = transition(
            snap(1, rabbit(1, 0)),
            snap(9, rabbit(1, 3), rabbit(2, 1)),
            paused,
            new Map(),
        );
        const frame = frameAt(jump, 150, open);
        expect(frame[0]).toMatchObject({ id: 1, x: 3, alpha: 1 });
        expect(frame[1]).toMatchObject({ id: 2, alpha: 0.5 });
    });

    test("a move into a group slides its nudge too", () => {
        const move = transition(
            snap(1, rabbit(1, 0)),
            snap(2, rabbit(1, 1, -3), rabbit(2, 1, 3)),
            paused,
            new Map(),
        );
        expect(frameAt(move, 210, open)[0]).toMatchObject({ x: 0.5, ox: -1.5 });
    });

    test("a rabbit moving left is flipped, and keeps it standing", () => {
        const left = transition(snap(1, rabbit(1, 2)), snap(2, rabbit(1, 1)), paused, new Map());
        expect(frameAt(left, 0, open)[0].flipped).toBe(true);
        const still = transition(snap(2, rabbit(1, 1)), snap(3, rabbit(1, 1)), paused, left.facing);
        expect(frameAt(still, 0, open)[0].flipped).toBe(true);
    });

    test("a standing creature blinks, a moving one does not", () => {
        const shut = Array.from({ length: 500 }, (_, i) => i * 10).find((now) => blinking(1, now))!;
        const still = transition(snap(1, rabbit(1, 0)), snap(2, rabbit(1, 0)), paused, new Map());
        expect(frameAt(still, 1000, shut)[0].frame).toBe("blink");
        const move = transition(snap(1, rabbit(1, 0)), snap(2, rabbit(1, 1)), paused, new Map());
        expect(frameAt(move, 210, shut)[0].frame).toBe("air");
    });

    test("a gone id fades out over the fade", () => {
        const gone = transition(snap(1, rabbit(1, 0)), snap(2), paused, new Map());
        expect(frameAt(gone, 150, open)[0]).toMatchObject({ id: 1, alpha: 0.5 });
        expect(frameAt(gone, 300, open)).toEqual([]);
    });

    test("a rabbit joining a full tile is drawn on its way and hidden once it lands", () => {
        const before = snap(1, rabbit(9, 0), rabbit(2, 1, -4), rabbit(3, 1, 4), rabbit(4, 1));
        const after: Moment = {
            ...snap(2, rabbit(9, 1), rabbit(2, 1, -4), rabbit(3, 1, 4), rabbit(4, 1)),
            drawn: new Set([2, 3, 4]),
        };
        const move = transition(before, after, paused, new Map());
        expect(frameAt(move, 210, open).map((s) => s.id)).toContain(9);
        expect(frameAt(move, 420, open).map((s) => s.id)).not.toContain(9);
    });

    test("grass never poses or blinks", () => {
        const tuft: Placed = {
            id: 1,
            spriteKey: "grass",
            creature: false,
            x: 0,
            y: 0,
            ox: 0,
            oy: 0,
        };
        const shut = Array.from({ length: 500 }, (_, i) => i * 10).find((now) => blinking(1, now))!;
        const still = transition(snap(1, tuft), snap(2, tuft), paused, new Map());
        expect(frameAt(still, 0, shut)[0].frame).toBe("stand");
    });

    test("a creature nudged on its own tile slides in its standing pose, even when a rabbit there is gone", () => {
        const wolf = creature(1, "wolf", 1);
        const move = transition(
            snap(1, { ...wolf, ox: -3 }, rabbit(2, 1, 3)),
            snap(2, wolf),
            paused,
            new Map(),
        );
        const mid = frameAt(move, 100, open).find((s) => s.id === 1);
        expect(mid).toMatchObject({ frame: "stand", lift: 0, x: 1 });
        expect(mid?.ox).toBeGreaterThan(-3);
        expect(mid?.ox).toBeLessThan(0);
    });

    test("a walking wolf does not blink mid-stride, and blinks once it stands", () => {
        const shut = Array.from({ length: 500 }, (_, i) => i * 10).find((now) => blinking(1, now))!;
        const walk = transition(
            snap(1, creature(1, "wolf", 0)),
            snap(2, creature(1, "wolf", 1)),
            paused,
            new Map(),
        );
        expect(frameAt(walk, 252, shut)[0].frame).toBe("stand");
        expect(frameAt(walk, 420, shut)[0].frame).toBe("blink");
    });

    test("a creature that faced left still faces left while it fades out", () => {
        const left = transition(snap(1, rabbit(1, 2)), snap(2, rabbit(1, 1)), paused, new Map());
        const gone = transition(snap(2, rabbit(1, 1)), snap(3), paused, left.facing);
        expect(frameAt(gone, 150, open)[0]).toMatchObject({ id: 1, flipped: true });
    });

    test("a creature pushed out of a group fades out, and one let into it fades in", () => {
        const four = [rabbit(2, 1), rabbit(3, 1), rabbit(4, 1), rabbit(5, 1)];
        const pushed = transition(
            { ...snap(1, ...four), drawn: new Set([3, 4, 5]) },
            { ...snap(2, ...four), drawn: new Set([2, 3, 4]) },
            paused,
            new Map(),
        );
        const frame = frameAt(pushed, 150, open);
        expect(frame.find((s) => s.id === 5)).toMatchObject({ alpha: 0.5 });
        expect(frame.find((s) => s.id === 2)).toMatchObject({ alpha: 0.5 });
        expect(frameAt(pushed, 300, open).map((s) => s.id)).toEqual([2, 3, 4]);
    });
});

describe("settling", () => {
    const snap = (day: number, ...placed: Placed[]): Moment => ({
        day,
        placed: new Map(placed.map((p) => [p.id, p])),
        drawn: new Set(placed.map((p) => p.id)),
    });
    const paused = { playing: false, daysPerSecond: 1 };

    test("comes when the longest move of the day has landed", () => {
        const day = transition(
            snap(1, rabbit(1, 0), rabbit(2, 0)),
            snap(2, rabbit(1, 1), rabbit(2, 3)),
            paused,
            new Map(),
        );
        expect(settledMs(day)).toBe(740);
    });

    test("is at once when nothing moves", () => {
        const still = transition(snap(1, rabbit(1, 0)), snap(2, rabbit(1, 0)), paused, new Map());
        expect(settledMs(still)).toBe(0);
    });
});
