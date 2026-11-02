import { describe, expect, test } from "vitest";
import { wholeScale } from "./scale";

describe("the world's scale", () => {
    test("is the largest whole number at which the world fits beside the side column", () => {
        // 1280 - 400 = 880 wide and 900 - 180 = 720 high, over 320 art pixels: 2.75 and 2.25.
        expect(wholeScale(20, 20, 1280, 900)).toBe(2);
        expect(wholeScale(20, 20, 2000, 1400)).toBe(3);
    });

    test("never drops below 1 or rises above 4", () => {
        expect(wholeScale(20, 20, 300, 200)).toBe(1);
        expect(wholeScale(5, 5, 4000, 3000)).toBe(4);
    });

    test("is always a whole number", () => {
        for (let width = 500; width < 3000; width += 37) {
            expect(Number.isInteger(wholeScale(17, 11, width, width * 0.7))).toBe(true);
        }
    });

    test("leaves a tile of headroom above the world for a hop on the top row", () => {
        // 21 tiles of 16 art pixels at 3x is 1008, so 180 + 1008 high fits 3x and one less does not.
        expect(wholeScale(20, 20, 3000, 180 + 1008)).toBe(3);
        expect(wholeScale(20, 20, 3000, 180 + 1007)).toBe(2);
    });
});
