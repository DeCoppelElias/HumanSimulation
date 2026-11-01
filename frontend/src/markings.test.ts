import { describe, expect, test } from "vitest";
import { blend, marking, markedPalette, weight } from "./markings";
import type { GeneRange } from "./types";

const ranges: GeneRange[] = [
    { key: "viewRange", min: 1, max: 10, baseline: 3 },
    { key: "whim", min: 0.05, max: 5, baseline: 0.5 },
    { key: "speed", min: 0, max: 1, baseline: 0.5 },
    { key: "breedPull", min: 0, max: 5, baseline: 1 },
    { key: "fixed", min: 2, max: 2, baseline: 2 },
];
const baseline = Object.fromEntries(ranges.map((range) => [range.key, range.baseline]));

/** A genome pushed a share of each range along a channel's weights, or against them. */
function pushed(channel: string, share: number): Record<string, number> {
    return Object.fromEntries(
        ranges.map((range) => {
            const step =
                Math.sign(weight("rabbit", channel, range.key)) * share * (range.max - range.min);
            return [range.key, Math.min(range.max, Math.max(range.min, range.baseline + step))];
        }),
    );
}

describe("weights", () => {
    test("are fixed by species, channel and gene, whatever the order", () => {
        expect(weight("rabbit", "colour", "whim")).toBe(weight("rabbit", "colour", "whim"));
        expect(weight("rabbit", "colour", "whim")).not.toBe(weight("rabbit", "extent", "whim"));
        expect(weight("rabbit", "colour", "whim")).not.toBe(weight("wolf", "colour", "whim"));
        expect(blend("rabbit", "colour", pushed("colour", 0.2), ranges)).toBeCloseTo(
            blend("rabbit", "colour", pushed("colour", 0.2), [...ranges].reverse()),
            12,
        );
    });

    test("spread like a unit normal over many genes", () => {
        const values = Array.from({ length: 2000 }, (_, i) => weight("rabbit", "colour", `g${i}`));
        const mean = values.reduce((a, b) => a + b, 0) / values.length;
        const sd = Math.sqrt(values.reduce((a, b) => a + (b - mean) ** 2, 0) / values.length);
        expect(Math.abs(mean)).toBeLessThan(0.1);
        expect(sd).toBeGreaterThan(0.9);
        expect(sd).toBeLessThan(1.1);
    });
});

describe("a marking", () => {
    test("puts the founder in the middle of every channel", () => {
        expect(blend("rabbit", "extent", baseline, ranges)).toBe(0.5);
        expect(marking("rabbit", baseline, ranges)).toEqual({ colour: 3, extent: 2, accent: 1 });
    });

    test("a genome far along a channel's weights reaches its last option, and far against them its first", () => {
        expect(marking("rabbit", pushed("extent", 0.4), ranges).extent).toBe(3);
        expect(marking("rabbit", pushed("extent", -0.4), ranges).extent).toBe(0);
        expect(marking("rabbit", pushed("accent", 0.4), ranges).accent).toBe(2);
        expect(marking("rabbit", pushed("colour", -0.4), ranges).colour).toBe(0);
    });

    test("scales each gene by its range, so a wide gene does not swamp a narrow one", () => {
        // Undo tanh and the weight: 10% of either range must pull the same amount.
        const pull = (genes: Record<string, number>, key: string) =>
            Math.atanh(2 * blend("rabbit", "colour", genes, ranges) - 1) /
            weight("rabbit", "colour", key);
        expect(pull({ ...baseline, viewRange: 3 + 0.9 }, "viewRange")).toBeCloseTo(
            pull({ ...baseline, speed: 0.5 + 0.1 }, "speed"),
            9,
        );
    });

    test("a small step from a parent moves the blend only a little", () => {
        const child = { ...baseline, whim: baseline.whim + 0.05 };
        expect(Math.abs(blend("rabbit", "colour", child, ranges) - 0.5)).toBeLessThan(0.02);
    });

    test("a gene with no range adds nothing", () => {
        expect(blend("rabbit", "colour", { ...baseline, fixed: 2 }, ranges)).toBe(0.5);
    });

    test("a gene the genome lacks counts as its baseline", () => {
        const missing = Object.fromEntries(
            Object.entries(baseline).filter(([key]) => key !== "whim"),
        );
        expect(blend("rabbit", "colour", missing, ranges)).toBe(0.5);
    });
});

describe("a marked palette", () => {
    const shade = (n: number) => ({
        base: { w: `#00000${n}` },
        marked: { t: `#10000${n}`, h: `#20000${n}`, f: `#30000${n}` },
    });
    const kind = {
        palette: { o: "#1a1f2b", w: "#ffffff", t: "#ffffff", h: "#ffffff", f: "#ffffff" },
        marks: {
            colours: [1, 2, 3, 4, 5, 6].map(shade),
            extents: [[], ["t"], ["t", "h"], ["t", "h"]],
            accents: [[], ["f"], []],
        },
    };

    test("tints the coat with the chosen shade and paints the named regions its marked colours", () => {
        expect(markedPalette(kind, { colour: 2, extent: 1, accent: 1 })).toEqual({
            o: "#1a1f2b",
            w: "#000003",
            t: "#100003",
            h: "#ffffff",
            f: "#300003",
        });
    });

    test("with no extent or accent only tints the coat", () => {
        expect(markedPalette(kind, { colour: 0, extent: 0, accent: 0 })).toEqual({
            ...kind.palette,
            w: "#000001",
        });
    });
});
