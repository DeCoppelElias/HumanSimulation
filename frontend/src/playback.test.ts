import { describe, expect, test } from "vitest";
import { advance, START } from "./playback";
import type { EntityView, WorldSnapshot } from "./types";

const rabbit = (id: number, genes: Record<string, number> = {}): EntityView => ({
    id,
    species: "rabbit",
    spriteKey: "rabbit",
    creature: true,
    info: {},
    genes,
});
const world = (seed: number, day: number, ...at: [EntityView, number][]): WorldSnapshot => ({
    seed,
    day,
    width: 4,
    height: 1,
    species: ["rabbit"],
    settings: {},
    tiles: [0, 1, 2, 3].map((x) => ({
        at: { x, y: 0 },
        fields: {},
        entities: at.filter(([, tx]) => tx === x).map(([entity]) => entity),
    })),
});
const paused = { playing: false, daysPerSecond: 1 };

describe("playback", () => {
    test("the first snapshot starts a run with nothing moving", () => {
        const step = advance(START, world(1, 5, [rabbit(1), 0]), paused);
        expect(step.newRun).toBe(true);
        expect(step.transition.moves.size).toBe(0);
        expect(step.state.entities.get(1)?.species).toBe("rabbit");
    });

    test("the next day of the same run moves from where the last left off and keeps facing", () => {
        const first = advance(START, world(1, 5, [rabbit(1), 2]), paused);
        const left = advance(first.state, world(1, 6, [rabbit(1), 1]), paused);
        expect(left.newRun).toBe(false);
        expect(left.transition.moves.get(1)?.tiles).toBe(1);
        const still = advance(left.state, world(1, 7, [rabbit(1), 1]), paused);
        expect(still.transition.facing.get(1)).toBe(true);
    });

    test("a reset starts a new run: nothing moves, and facing and entities start over", () => {
        const first = advance(START, world(1, 5, [rabbit(1), 2]), paused);
        const left = advance(first.state, world(1, 6, [rabbit(1), 1], [rabbit(2), 3]), paused);
        const reset = advance(left.state, world(9, 0, [rabbit(1), 3]), paused);
        expect(reset.newRun).toBe(true);
        expect(reset.transition.moves.size).toBe(0);
        expect(reset.transition.facing.get(1)).toBe(false);
        expect([...reset.state.entities.keys()]).toEqual([1]);
    });

    test("a day going backwards is a new run too", () => {
        const first = advance(START, world(1, 5, [rabbit(1), 0]), paused);
        expect(advance(first.state, world(1, 2, [rabbit(1), 0]), paused).newRun).toBe(true);
    });

    test("keeps a creature that just died while it fades, then lets it go", () => {
        const one = advance(START, world(1, 5, [rabbit(1, { speed: 0.7 }), 0]), paused);
        const died = advance(one.state, world(1, 6), paused);
        expect(died.state.entities.get(1)?.genes).toEqual({ speed: 0.7 });
        expect(died.gone).toEqual([]);
        const after = advance(died.state, world(1, 7), paused);
        expect(after.state.entities.has(1)).toBe(false);
        expect(after.gone).toEqual([1]);
    });
});
