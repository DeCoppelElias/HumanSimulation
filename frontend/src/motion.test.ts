import { describe, expect, test } from "vitest";
import { finished, frameAt, timing, type Transition } from "./motion";
import type { Placed } from "./tiles";

const placed = (id: number, x: number, y: number): Placed => ({
    id,
    spriteKey: "rabbit",
    creature: true,
    x,
    y,
});
const map = (...items: Placed[]) => new Map(items.map((item) => [item.id, item]));
const ids = (...values: number[]) => new Set(values);

describe("timing", () => {
    test("one day while playing glides over one day at the rate", () => {
        expect(timing(1, { playing: true, daysPerSecond: 2 })).toEqual({
            glideMs: 500,
            fadeMs: 500,
        });
    });

    test("one day stepped while paused glides over 300 ms", () => {
        expect(timing(1, { playing: false, daysPerSecond: 2 })).toEqual({
            glideMs: 300,
            fadeMs: 300,
        });
    });

    test("a jump or no day at all snaps and still fades", () => {
        expect(timing(5, { playing: true, daysPerSecond: 2 })).toEqual({ glideMs: 0, fadeMs: 300 });
        expect(timing(0, { playing: false, daysPerSecond: 2 })).toEqual({
            glideMs: 0,
            fadeMs: 300,
        });
    });
});

describe("a frame", () => {
    const glide: Transition = {
        from: map(placed(1, 0, 0)),
        to: map(placed(1, 2, 0)),
        fromDrawn: ids(1),
        toDrawn: ids(1),
        timing: { glideMs: 500, fadeMs: 500 },
    };

    test("a moved rabbit glides in a straight line", () => {
        expect(frameAt(glide, 0)).toEqual([{ ...placed(1, 0, 0), alpha: 1 }]);
        expect(frameAt(glide, 250)).toEqual([{ ...placed(1, 1, 0), alpha: 1 }]);
        expect(frameAt(glide, 900)).toEqual([{ ...placed(1, 2, 0), alpha: 1 }]);
    });

    test("with no glide it is at its new tile at once", () => {
        const snap = { ...glide, timing: { glideMs: 0, fadeMs: 300 } };
        expect(frameAt(snap, 0)).toEqual([{ ...placed(1, 2, 0), alpha: 1 }]);
    });

    test("a new id fades in and a gone one fades out and is dropped", () => {
        const swap: Transition = {
            from: map(placed(1, 0, 0)),
            to: map(placed(2, 1, 0)),
            fromDrawn: ids(1),
            toDrawn: ids(2),
            timing: { glideMs: 0, fadeMs: 300 },
        };
        expect(frameAt(swap, 150)).toEqual([
            { ...placed(1, 0, 0), alpha: 0.5 },
            { ...placed(2, 1, 0), alpha: 0.5 },
        ]);
        expect(frameAt(swap, 300)).toEqual([{ ...placed(2, 1, 0), alpha: 1 }]);
        expect(finished(swap, 299)).toBe(false);
        expect(finished(swap, 300)).toBe(true);
    });

    test("a rabbit walking onto a lower id glides there and then hides under it", () => {
        const cover: Transition = {
            from: map(placed(1, 0, 0), placed(3, 1, 0)),
            to: map(placed(1, 1, 0), placed(3, 1, 0)),
            fromDrawn: ids(1, 3),
            toDrawn: ids(1),
            timing: { glideMs: 500, fadeMs: 500 },
        };
        expect(frameAt(cover, 250).map((sprite) => sprite.id)).toEqual([1, 3]);
        expect(frameAt(cover, 500).map((sprite) => sprite.id)).toEqual([1]);
    });

    test("a rabbit that was hidden in a stack is not drawn while still covered", () => {
        const hidden: Transition = {
            from: map(placed(1, 0, 0), placed(3, 0, 0)),
            to: map(placed(1, 0, 0), placed(3, 0, 0)),
            fromDrawn: ids(1),
            toDrawn: ids(1),
            timing: { glideMs: 500, fadeMs: 500 },
        };
        expect(frameAt(hidden, 0).map((sprite) => sprite.id)).toEqual([1]);
    });
});
