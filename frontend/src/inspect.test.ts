import { describe, expect, test } from "vitest";
import {
    afterClick,
    creatureRows,
    formatValue,
    goneMessage,
    inspectTile,
    label,
    resolve,
    sight,
    sightOf,
    sortRows,
} from "./inspect";
import type { EntityView, WorldSnapshot } from "./types";

const rabbit = (id: number, age: number, reserve: number): EntityView => ({
    id,
    species: "rabbit",
    spriteKey: "rabbit",
    creature: true,
    info: { age, reserve, daysToStarve: reserve - 1, amount: 30 },
    genes: { viewRange: 3 },
});
const grass = (id: number): EntityView => ({
    id,
    species: "grass",
    spriteKey: "grass",
    creature: false,
    info: { age: 1, amount: 15, daysToLive: 10 },
    genes: {},
});

const snapshot: WorldSnapshot = {
    seed: 1,
    day: 5,
    width: 3,
    height: 1,
    species: ["grass", "rabbit"],
    settings: {},
    tiles: [
        { at: { x: 0, y: 0 }, fields: {}, entities: [rabbit(4, 9, 2)] },
        { at: { x: 1, y: 0 }, fields: {}, entities: [rabbit(7, 3, 12), grass(8)] },
        { at: { x: 2, y: 0 }, fields: {}, entities: [] },
    ],
};

describe("resolve", () => {
    test("finds the picked id and where it stands", () => {
        expect(resolve(snapshot, { id: 7, species: "rabbit" })).toEqual({
            kind: "alive",
            entity: rabbit(7, 3, 12),
            at: { x: 1, y: 0 },
        });
    });
    test("an id that no longer resolves is gone, not an error", () => {
        expect(resolve(snapshot, { id: 99, species: "rabbit" })).toEqual({
            kind: "gone",
            picked: { id: 99, species: "rabbit" },
        });
    });
    test("nothing picked is nothing", () => {
        expect(resolve(snapshot, null)).toEqual({ kind: "none" });
    });
});

describe("inspectTile", () => {
    test("a lone entity is selected", () => {
        expect(inspectTile(snapshot, { x: 0, y: 0 })).toEqual({
            entities: [rabbit(4, 9, 2)],
            select: { id: 4, species: "rabbit" },
        });
    });
    test("a rabbit on grass is a list, since grass counts", () => {
        expect(inspectTile(snapshot, { x: 1, y: 0 })).toEqual({
            entities: [rabbit(7, 3, 12), grass(8)],
            select: null,
        });
    });
    test("an empty tile has nothing", () => {
        expect(inspectTile(snapshot, { x: 2, y: 0 })).toEqual({ entities: [], select: null });
    });
});

describe("afterClick", () => {
    const before = { id: 4, species: "rabbit" };
    test("an empty tile clears the selection", () => {
        expect(afterClick(before, inspectTile(snapshot, { x: 2, y: 0 }))).toBeNull();
    });
    test("a crowded tile keeps the selection until a row is picked", () => {
        expect(afterClick(before, inspectTile(snapshot, { x: 1, y: 0 }))).toEqual(before);
    });
    test("a lone entity replaces it", () => {
        expect(afterClick(null, inspectTile(snapshot, { x: 0, y: 0 }))).toEqual(before);
    });
});

describe("sightOf", () => {
    test("a selected creature sees its circle", () => {
        expect(sightOf(snapshot, resolve(snapshot, { id: 4, species: "rabbit" }))).toEqual([
            { x: 0, y: 0 },
            { x: 1, y: 0 },
            { x: 2, y: 0 },
        ]);
    });
    test("an entity that is no creature sees nothing, whatever its genes", () => {
        const seeing = { ...grass(8), genes: { viewRange: 3 } };
        expect(sightOf(snapshot, { kind: "alive", entity: seeing, at: { x: 1, y: 0 } })).toEqual(
            [],
        );
    });
    test("a creature without the gene, and no selection, see nothing", () => {
        const blind = { ...rabbit(4, 9, 2), genes: {} };
        expect(sightOf(snapshot, { kind: "alive", entity: blind, at: { x: 0, y: 0 } })).toEqual([]);
        expect(sightOf(snapshot, { kind: "none" })).toEqual([]);
    });
});

describe("sight", () => {
    const open = { x: 5, y: 5 };
    test("range 3 in the open is 29 tiles, as the grid's circle", () => {
        expect(sight(open, 3, 11, 11)).toHaveLength(29);
    });
    test("range 1 in a corner is 3 tiles", () => {
        expect(sight({ x: 0, y: 0 }, 1, 11, 11)).toEqual([
            { x: 0, y: 0 },
            { x: 1, y: 0 },
            { x: 0, y: 1 },
        ]);
    });
    test("the gene rounds, half up, before the circle", () => {
        expect(sight(open, 2.5, 11, 11)).toHaveLength(29);
        expect(sight(open, 2.4, 11, 11)).toHaveLength(13);
    });
    test("a diagonal two away is outside range 2", () => {
        expect(sight(open, 2, 11, 11)).not.toContainEqual({ x: 7, y: 7 });
    });
});

describe("creatureRows and sortRows", () => {
    const rows = creatureRows(snapshot);
    test("lists creatures only", () => {
        expect(rows.map((row) => row.id)).toEqual([4, 7]);
    });
    test("sorts by age, largest first", () => {
        expect(sortRows(rows, "age").map((row) => row.id)).toEqual([4, 7]);
    });
    test("sorts by reserve, largest first", () => {
        expect(sortRows(rows, "reserve").map((row) => row.id)).toEqual([7, 4]);
    });
    test("leaves the frozen rows as they were", () => {
        sortRows(rows, "reserve");
        expect(rows.map((row) => row.id)).toEqual([4, 7]);
    });
    test("equal values keep ids ascending", () => {
        const tied = creatureRows({
            ...snapshot,
            tiles: [
                {
                    at: { x: 0, y: 0 },
                    fields: {},
                    entities: [rabbit(9, 1, 1), rabbit(2, 1, 1)],
                },
            ],
        });
        expect(sortRows(tied, "age").map((row) => row.id)).toEqual([2, 9]);
    });
});

describe("text", () => {
    test("labels name the value and fall back to the key", () => {
        expect(label("daysToStarve")).toBe("Days to starve");
        expect(label("somethingNew")).toBe("somethingNew");
    });
    test("whole numbers stay whole and others get one decimal", () => {
        expect(formatValue(12)).toBe("12");
        expect(formatValue(13.86)).toBe("13.9");
    });
    test("a gone creature is named by species and id", () => {
        expect(goneMessage({ id: 812, species: "rabbit" })).toBe("Rabbit 812 is no longer alive.");
    });
});
