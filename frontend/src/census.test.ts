import { describe, expect, test } from "vitest";
import { firstGap, merge, receive, refreshed, speciesIn, toSeries } from "./census";
import type { CensusRow } from "./types";

const row = (day: number, rabbit = 10): CensusRow => ({
    day,
    population: { rabbit },
    geneMeans: {},
});

describe("the census history", () => {
    test("the next day appends", () => {
        expect(receive([row(1), row(2)], row(3))).toEqual({
            history: [row(1), row(2), row(3)],
            missingFrom: null,
        });
    });

    test("a skipped day asks for the gap", () => {
        expect(receive([row(1), row(2)], row(6))).toEqual({
            history: [row(1), row(2), row(6)],
            missingFrom: 3,
        });
    });

    test("a first row after day 1 asks for everything before it", () => {
        expect(receive([], row(40)).missingFrom).toBe(1);
        expect(receive([], row(1)).missingFrom).toBeNull();
    });

    test("a row already held changes nothing", () => {
        const history = [row(1), row(2)];
        expect(receive(history, row(2))).toEqual({ history, missingFrom: null });
    });

    test("fetched rows merge in day order without duplicates", () => {
        expect(merge([row(1), row(6)], [row(2), row(3), row(6, 99)])).toEqual([
            row(1),
            row(2),
            row(3),
            row(6, 99),
        ]);
    });

    test("a refresh takes the server's rows up to its day and keeps newer ones held", () => {
        const held = [row(1), row(2), row(3), row(9), row(10)];
        expect(refreshed(held, [row(8), row(9, 50)], 9)).toEqual([row(8), row(9, 50), row(10)]);
    });

    test("a refresh after a statistics reset with no rows yet drops what was held", () => {
        expect(refreshed([row(1), row(2)], [], 2)).toEqual([]);
    });

    test("the first gap is the first day missing between two held rows", () => {
        expect(firstGap([row(1), row(2), row(5), row(6), row(9)])).toBe(3);
        expect(firstGap([row(1), row(2), row(3)])).toBeNull();
        expect(firstGap([])).toBeNull();
    });

    test("a history starting after day 1 has no gap, since a statistics reset starts it there", () => {
        expect(firstGap([row(40), row(41)])).toBeNull();
    });

    test("series are a day column and one column per species", () => {
        const history: CensusRow[] = [
            { day: 1, population: { rabbit: 10, wolf: 1 }, geneMeans: {} },
            { day: 2, population: { rabbit: 12 }, geneMeans: {} },
        ];
        expect(speciesIn(history)).toEqual(["rabbit", "wolf"]);
        expect(toSeries(history, ["rabbit", "wolf"])).toEqual([
            [1, 2],
            [10, 12],
            [1, 0],
        ]);
    });
});
