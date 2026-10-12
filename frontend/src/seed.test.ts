import { describe, expect, test } from "vitest";
import { readSeed } from "./seed";

describe("the seed input", () => {
    test("blank draws from the clock", () => {
        expect(readSeed("  ")).toEqual({ kind: "clock" });
    });

    test("a whole number is that seed", () => {
        expect(readSeed("42")).toEqual({ kind: "seed", seed: 42 });
        expect(readSeed("-7")).toEqual({ kind: "seed", seed: -7 });
        expect(readSeed("9007199254740991")).toEqual({ kind: "seed", seed: 9007199254740991 });
    });

    test("anything else is refused with a reason", () => {
        expect(readSeed("4.2").kind).toBe("invalid");
        expect(readSeed("abc").kind).toBe("invalid");
        expect(readSeed("9007199254740992").kind).toBe("invalid");
    });
});
