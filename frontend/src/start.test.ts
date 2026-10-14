import { describe, expect, test } from "vitest";
import { populate } from "./start";
import type { Command } from "./types";

describe("start", () => {
    test("a new or reset world gets ten rabbits and seven grass", async () => {
        const sent: Command[] = [];
        await populate(
            {
                command: (_id: string, command: Command) => {
                    sent.push(command);
                    return Promise.resolve(null);
                },
            },
            "w",
        );
        expect(sent).toEqual([
            { type: "spawn", species: "rabbit", count: 10 },
            { type: "spawn", species: "grass", count: 7 },
        ]);
    });
});
