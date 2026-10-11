import { describe, expect, test } from "vitest";
import { addressFor, worldFromAddress } from "./address";

describe("the world's address", () => {
    test("reads the world from the query", () => {
        expect(worldFromAddress("?world=abc123")).toBe("abc123");
    });

    test("no parameter or an empty one is no world", () => {
        expect(worldFromAddress("")).toBeNull();
        expect(worldFromAddress("?world=")).toBeNull();
        expect(worldFromAddress("?other=1")).toBeNull();
    });

    test("writes the query for a world and the root for none", () => {
        expect(addressFor("abc123")).toBe("/?world=abc123");
        expect(addressFor(null)).toBe("/");
    });
});
