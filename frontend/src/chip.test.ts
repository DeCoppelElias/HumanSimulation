import { describe, expect, test } from "vitest";
import { chip } from "./chip";

describe("the count chip", () => {
    test("is absent for a lone creature", () => {
        expect(chip(1)).toBeNull();
        expect(chip(0)).toBeNull();
    });

    test("is a white digit on ink with a one-pixel margin", () => {
        expect(chip(2)).toEqual(["ooooo", "owwwo", "ooowo", "owwwo", "owooo", "owwwo", "ooooo"]);
    });

    test("widens for two digits, with a gap between them", () => {
        const grid = chip(12);
        expect(grid).toHaveLength(7);
        expect(grid?.[0]).toHaveLength(9);
        expect(grid?.[1]).toBe("oowoowwwo");
    });
});
