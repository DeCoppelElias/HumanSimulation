import { describe, expect, test } from "vitest";
import { isNewRun } from "./run";
import type { WorldSnapshot } from "./types";

const at = (seed: number, day: number): WorldSnapshot => ({
    seed,
    day,
    width: 1,
    height: 1,
    species: [],
    tiles: [],
    settings: {},
});

describe("a new run", () => {
    test("the next day of the same seed is the same run", () => {
        expect(isNewRun(at(42, 3), at(42, 4))).toBe(false);
        expect(isNewRun(at(42, 3), at(42, 3))).toBe(false);
        expect(isNewRun(at(42, 3), at(42, 500))).toBe(false);
    });

    test("a changed seed starts a new run", () => {
        expect(isNewRun(at(42, 3), at(7, 3))).toBe(true);
    });

    test("a day going backwards starts a new run", () => {
        expect(isNewRun(at(42, 9), at(42, 0))).toBe(true);
    });
});
