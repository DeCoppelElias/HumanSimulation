import { describe, expect, test } from "vitest";
import { drawn, placements, stacks } from "./tiles";
import type { WorldSnapshot } from "./types";

const rabbit = (id: number) => ({ id, species: "rabbit", spriteKey: "rabbit", info: {} });

const snapshot: WorldSnapshot = {
    seed: 1,
    day: 0,
    width: 3,
    height: 1,
    tiles: [
        { at: { x: 0, y: 0 }, fields: {}, entities: [rabbit(4), rabbit(2), rabbit(9)] },
        { at: { x: 1, y: 0 }, fields: {}, entities: [] },
        { at: { x: 2, y: 0 }, fields: {}, entities: [rabbit(5)] },
    ],
};

describe("tiles", () => {
    test("places every entity by id", () => {
        expect(placements(snapshot).get(9)).toEqual({ id: 9, spriteKey: "rabbit", x: 0, y: 0 });
        expect(placements(snapshot).size).toBe(4);
    });

    test("a shared tile shows its lowest id and a count", () => {
        expect(stacks(snapshot)).toEqual([
            { x: 0, y: 0, topId: 2, count: 3 },
            { x: 2, y: 0, topId: 5, count: 1 },
        ]);
    });

    test("only the top of each tile is drawn", () => {
        expect([...drawn(snapshot)].sort((a, b) => a - b)).toEqual([2, 5]);
    });
});
