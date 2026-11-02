import { describe, expect, test } from "vitest";
import { lookOf } from "./looks";
import sheet from "./sprites.json";

const drawn = (spriteKey: string, frame: string, creature = true, id = 4) => ({
    id,
    spriteKey,
    creature,
    frame,
});

describe("a look", () => {
    test("is a creature's frame in its unmarked palette until it has a marking", () => {
        expect(lookOf(drawn("rabbit", "air"), null)).toEqual({
            key: "rabbit/air/plain",
            grid: sheet.rabbit.frames.air,
            palette: sheet.rabbit.palette,
        });
    });

    test("with a marking is keyed by it and painted with it", () => {
        const look = lookOf(drawn("rabbit", "air"), { colour: 3, extent: 2, accent: 1 });
        expect(look?.key).toBe("rabbit/air/3.2.1");
        const colour = sheet.rabbit.marks.colours[3].marked.t;
        expect(look?.palette).toMatchObject({ t: colour, h: colour, f: colour, c: "#ffffff" });
    });

    test("falls back to standing for a frame the species does not draw", () => {
        expect(lookOf(drawn("rabbit", "stride"), null)?.grid).toBe(sheet.rabbit.frames.stand);
        expect(lookOf(drawn("rabbit", "stride"), null)?.key).toBe("rabbit/stand/plain");
    });

    test("for grass is a tuft picked by its id", () => {
        expect(lookOf(drawn("grass", "stand", false, 5), null)).toEqual({
            key: "grass/2",
            grid: sheet.grass.tufts[2],
            palette: sheet.grass.palette,
        });
    });

    test("is missing for a species with no art", () => {
        expect(lookOf(drawn("deer", "stand"), null)).toBeNull();
        expect(lookOf(drawn("mushroom", "stand", false), null)).toBeNull();
    });
});
