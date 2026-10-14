import { describe, expect, test } from "vitest";
import { defaultSpecies } from "./species";

describe("species", () => {
    test("rabbit is selected when the world knows it", () => {
        expect(defaultSpecies(["grass", "rabbit"])).toBe("rabbit");
    });

    test("otherwise the first species the world knows", () => {
        expect(defaultSpecies(["grass", "wolf"])).toBe("grass");
    });

    test("a world with no species selects nothing", () => {
        expect(defaultSpecies([])).toBeNull();
    });
});
