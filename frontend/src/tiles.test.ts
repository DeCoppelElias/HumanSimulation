import { describe, expect, test } from "vitest";
import { drawn, placements, stacks } from "./tiles";
import type { WorldSnapshot } from "./types";

const rabbit = (id: number) => ({
    id,
    species: "rabbit",
    spriteKey: "rabbit",
    creature: true,
    info: {},
});
const grass = (id: number) => ({
    id,
    species: "grass",
    spriteKey: "grass",
    creature: false,
    info: {},
});

const snapshot: WorldSnapshot = {
    seed: 1,
    day: 0,
    width: 4,
    height: 1,
    species: ["grass", "rabbit"],
    tiles: [
        { at: { x: 0, y: 0 }, fields: {}, entities: [rabbit(4), rabbit(2), rabbit(9)] },
        { at: { x: 1, y: 0 }, fields: {}, entities: [] },
        { at: { x: 2, y: 0 }, fields: {}, entities: [grass(1), rabbit(5), grass(3)] },
        { at: { x: 3, y: 0 }, fields: {}, entities: [grass(6)] },
    ],
};

describe("tiles", () => {
    test("places every entity by id, with whether it is a creature", () => {
        expect(placements(snapshot).get(9)).toEqual({
            id: 9,
            spriteKey: "rabbit",
            creature: true,
            x: 0,
            y: 0,
        });
        expect(placements(snapshot).get(1)?.creature).toBe(false);
        expect(placements(snapshot).size).toBe(7);
    });

    test("a tile shows its lowest ground id under its lowest creature id, and counts creatures", () => {
        expect(stacks(snapshot)).toEqual([
            { x: 0, y: 0, groundId: null, topId: 2, creatures: 3 },
            { x: 2, y: 0, groundId: 1, topId: 5, creatures: 1 },
            { x: 3, y: 0, groundId: 6, topId: null, creatures: 0 },
        ]);
    });

    test("the ground and the top of each tile are drawn", () => {
        expect([...drawn(snapshot)].sort((a, b) => a - b)).toEqual([1, 2, 5, 6]);
    });
});
