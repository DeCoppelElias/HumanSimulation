import { describe, expect, test } from "vitest";
import { depth, drawn, groundVariant, nudges, placements, stacks, tuftOf } from "./tiles";
import type { WorldSnapshot } from "./types";

const rabbit = (id: number) => ({
    id,
    species: "rabbit",
    spriteKey: "rabbit",
    creature: true,
    info: {},
    genes: {},
});
const grass = (id: number) => ({
    id,
    species: "grass",
    spriteKey: "grass",
    creature: false,
    info: {},
    genes: {},
});

const snapshot: WorldSnapshot = {
    seed: 1,
    day: 0,
    width: 4,
    height: 1,
    species: ["grass", "rabbit"],
    settings: {},
    tiles: [
        { at: { x: 0, y: 0 }, fields: {}, entities: [rabbit(4), rabbit(2), rabbit(9)] },
        { at: { x: 1, y: 0 }, fields: {}, entities: [] },
        { at: { x: 2, y: 0 }, fields: {}, entities: [grass(1), rabbit(5), grass(3)] },
        { at: { x: 3, y: 0 }, fields: {}, entities: [grass(6)] },
    ],
};

describe("tiles", () => {
    test("places every entity by id, with whether it is a creature and its nudge", () => {
        expect(placements(snapshot).get(9)).toEqual({
            id: 9,
            spriteKey: "rabbit",
            creature: true,
            x: 0,
            y: 0,
            ox: 0,
            oy: -2,
        });
        expect(placements(snapshot).get(1)).toMatchObject({ creature: false, ox: 0, oy: 0 });
        expect(placements(snapshot).size).toBe(7);
    });

    test("a tile shows its lowest ground id and up to three creatures by id, and counts them", () => {
        expect(stacks(snapshot)).toEqual([
            { x: 0, y: 0, groundId: null, shown: [2, 4, 9], creatures: 3 },
            { x: 2, y: 0, groundId: 1, shown: [5], creatures: 1 },
            { x: 3, y: 0, groundId: 6, shown: [], creatures: 0 },
        ]);
    });

    test("the ground and every shown creature are drawn", () => {
        expect([...drawn(snapshot)].sort((a, b) => a - b)).toEqual([1, 2, 4, 5, 6, 9]);
    });
});

describe("groups", () => {
    const crowded: WorldSnapshot = {
        ...snapshot,
        tiles: [
            {
                at: { x: 0, y: 0 },
                fields: {},
                entities: [rabbit(9), rabbit(4), rabbit(7), rabbit(2)],
            },
        ],
    };

    test("draw the three lowest ids, nudged apart, and count all four", () => {
        const placed = placements(crowded);
        expect([...drawn(crowded)].sort((a, b) => a - b)).toEqual([2, 4, 7]);
        expect([2, 4, 7].map((id) => [placed.get(id)?.ox, placed.get(id)?.oy])).toEqual([
            [-4, 1],
            [4, 1],
            [0, -2],
        ]);
        expect(placed.get(9)).toMatchObject({ ox: 0, oy: 0 });
        expect(stacks(crowded)[0].creatures).toBe(4);
    });

    test("a pair sits side by side and a lone creature in the middle", () => {
        expect(nudges(1)).toEqual([[0, 0]]);
        expect(nudges(2)).toEqual([
            [-3, 0],
            [3, 0],
        ]);
    });

    test("grass under a group is still drawn", () => {
        const withGrass: WorldSnapshot = {
            ...snapshot,
            tiles: [{ at: { x: 0, y: 0 }, fields: {}, entities: [grass(1), rabbit(4), rabbit(2)] }],
        };
        expect([...drawn(withGrass)].sort((a, b) => a - b)).toEqual([1, 2, 4]);
    });
});

describe("depth", () => {
    const at = (spriteKey: string, y: number, oy: number) => ({ spriteKey, y, oy });

    test("a wolf on a tile is in front of a rabbit there, wherever the rabbit is nudged", () => {
        expect(depth(at("wolf", 3, -2))).toBeGreaterThan(depth(at("rabbit", 3, 1)));
    });

    test("a lower row is in front, whatever its species", () => {
        expect(depth(at("rabbit", 4, -2))).toBeGreaterThan(depth(at("wolf", 3, 1)));
    });

    test("holds for a creature halfway between rows", () => {
        expect(depth(at("rabbit", 3.5, 0))).toBeGreaterThan(depth(at("wolf", 3, 0)));
    });
});

describe("variants", () => {
    test("pick each of the four ground tiles somewhere, the same way every time", () => {
        const seen = new Set<number>();
        for (let x = 0; x < 20; x++) for (let y = 0; y < 20; y++) seen.add(groundVariant(x, y));
        expect([...seen].sort()).toEqual([0, 1, 2, 3]);
        expect(groundVariant(5, 7)).toBe(groundVariant(5, 7));
    });

    test("do not repeat along a row", () => {
        const row = Array.from({ length: 20 }, (_, x) => groundVariant(x, 4));
        expect(new Set(row).size).toBeGreaterThan(2);
    });

    test("give each grass one of three tufts by its id", () => {
        expect([tuftOf(3), tuftOf(4), tuftOf(5), tuftOf(6)]).toEqual([0, 1, 2, 0]);
    });
});
