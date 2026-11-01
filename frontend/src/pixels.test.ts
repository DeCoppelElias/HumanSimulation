import { describe, expect, test } from "vitest";
import { rasterize } from "./pixels";
import sheet from "./sprites.json";

describe("rasterize", () => {
    test("paints each letter its colour and leaves dots clear", () => {
        const pixels = rasterize(["o.", ".w"], { o: "#1a1f2b", w: "#ffffff" });
        expect(pixels.width).toBe(2);
        expect(pixels.height).toBe(2);
        expect([...pixels.data]).toEqual([
            0x1a, 0x1f, 0x2b, 255, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 255,
        ]);
    });

    test("refuses a letter the palette does not colour", () => {
        expect(() => rasterize(["oz"], { o: "#1a1f2b" })).toThrow("z");
    });

    test("refuses a ragged grid", () => {
        expect(() => rasterize(["oo", "o"], { o: "#1a1f2b" })).toThrow("row 1");
    });
});

describe("every grid in sprites.json", () => {
    const size = (grid: string[]) => [grid[0].length, grid.length];

    test("rasterizes with its palette", () => {
        for (const frame of Object.values(sheet.rabbit.frames))
            rasterize(frame, sheet.rabbit.palette);
        for (const frame of Object.values(sheet.wolf.frames)) rasterize(frame, sheet.wolf.palette);
        for (const tuft of sheet.grass.tufts) rasterize(tuft, sheet.grass.palette);
        for (const tile of sheet.ground.tiles) rasterize(tile, sheet.ground.palette);
    });

    test("sizes the rabbit 16 by 16, the wolf 24 by 18 and the ground 16 by 16", () => {
        for (const frame of Object.values(sheet.rabbit.frames))
            expect(size(frame)).toEqual([16, 16]);
        for (const frame of Object.values(sheet.wolf.frames)) expect(size(frame)).toEqual([24, 18]);
        for (const tile of sheet.ground.tiles) expect(size(tile)).toEqual([16, 16]);
    });

    test("gives every region a marking repaints a colour in each of six shades", () => {
        for (const kind of [sheet.rabbit, sheet.wolf]) {
            const regions = [...kind.marks.extents.flat(), ...kind.marks.accents.flat()];
            expect(kind.marks.colours).toHaveLength(6);
            expect(kind.marks.extents).toHaveLength(4);
            expect(kind.marks.accents).toHaveLength(3);
            for (const letter of regions) {
                expect(kind.palette).toHaveProperty(letter);
                for (const shade of kind.marks.colours) expect(shade.marked).toHaveProperty(letter);
            }
        }
    });

    test("has the frames the page asks for", () => {
        expect(Object.keys(sheet.rabbit.frames)).toEqual(
            expect.arrayContaining(["stand", "blink", "crouch", "air"]),
        );
        expect(Object.keys(sheet.wolf.frames)).toEqual(
            expect.arrayContaining([
                "stand",
                "blink",
                "stride",
                "gather",
                "takeoff",
                "flight",
                "land",
            ]),
        );
    });
});
