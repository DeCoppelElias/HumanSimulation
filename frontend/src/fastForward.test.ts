import { describe, expect, test } from "vitest";
import { fastForward, type Stepper } from "./fastForward";
import type { DayReport } from "./types";

const report = { census: { day: 100, population: {} } } as DayReport;

function stepper(calls: string[], stepFails = false): Stepper {
    const state = { playing: false, daysPerSecond: 2 };
    return {
        pause: async () => {
            calls.push("pause");
            return state;
        },
        play: async () => {
            calls.push("play");
            return state;
        },
        step: async (_id, days) => {
            calls.push(`step ${days}`);
            if (stepFails) {
                throw new Error("this world is already stepping");
            }
            return report;
        },
    };
}

describe("fast-forward", () => {
    test("a paused world just steps", async () => {
        const calls: string[] = [];
        expect(await fastForward(stepper(calls), "w", 100, false)).toBe(report);
        expect(calls).toEqual(["step 100"]);
    });

    test("a playing world pauses, steps and plays again", async () => {
        const calls: string[] = [];
        await fastForward(stepper(calls), "w", 100, true);
        expect(calls).toEqual(["pause", "step 100", "play"]);
    });

    test("a failed step still plays again", async () => {
        const calls: string[] = [];
        await expect(fastForward(stepper(calls, true), "w", 100, true)).rejects.toThrow(
            "already stepping",
        );
        expect(calls).toEqual(["pause", "step 100", "play"]);
    });

    test("days outside 1 to 10,000 are refused before anything is sent", async () => {
        const calls: string[] = [];
        await expect(fastForward(stepper(calls), "w", 0, false)).rejects.toThrow(RangeError);
        await expect(fastForward(stepper(calls), "w", 10_001, false)).rejects.toThrow(RangeError);
        await expect(fastForward(stepper(calls), "w", 2.5, false)).rejects.toThrow(RangeError);
        expect(calls).toEqual([]);
    });
});
