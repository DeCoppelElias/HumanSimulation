import { describe, expect, test } from "vitest";
import {
    RESTORE_ALL,
    anyEdited,
    currentValue,
    flip,
    formatValue,
    groups,
    isEdited,
    onStep,
    release,
    restoreOne,
    settle,
    shownValue,
    withoutKey,
} from "./settings";
import type { SettingDeclaration } from "./types";

const declaration = (
    owner: string,
    key: string,
    defaultValue = 1,
    step = 1,
    min = 0,
): SettingDeclaration => ({
    owner,
    key,
    shape: "scalar",
    description: `${key}.`,
    min,
    max: 100,
    step,
    defaultValue,
});

const perDrop = declaration("world", "grassPerDrop", 7);
const stride = declaration("rabbit", "stride", 3);
const charge = declaration("rabbit", "viewCharge", 0.03, 0.01);

describe("the settings panel", () => {
    test("groups keep the order declarations arrive in, the world first", () => {
        expect(groups([perDrop, stride, charge])).toEqual([
            { owner: "world", label: "World", settings: [perDrop] },
            { owner: "rabbit", label: "Rabbit", settings: [stride, charge] },
        ]);
    });

    test("a setting is edited when its value differs from its default", () => {
        const values = { rabbit: { stride: 2, viewCharge: 0.03 } };
        expect(isEdited(stride, values)).toBe(true);
        expect(isEdited(charge, values)).toBe(false);
        expect(anyEdited([perDrop, charge], values)).toBe(false);
        expect(anyEdited([perDrop, stride], values)).toBe(true);
    });

    test("a value the snapshot lacks reads as its default", () => {
        expect(currentValue(perDrop, {})).toBe(7);
        expect(isEdited(perDrop, {})).toBe(false);
    });

    test("the slider shows its draft, else the value sent, else the snapshot's", () => {
        const values = { rabbit: { stride: 2 } };
        expect(shownValue({}, {}, stride, values)).toBe(2);
        expect(shownValue({}, { "rabbit.stride": 4 }, stride, values)).toBe(4);
        expect(shownValue({ "rabbit.stride": 5 }, { "rabbit.stride": 4 }, stride, values)).toBe(5);
    });

    test("a release sends the draft on its step and holds it until the snapshot catches up", () => {
        const values = { rabbit: { viewCharge: 0.05 } };
        expect(release({ "rabbit.viewCharge": 0.060000000000000005 }, {}, charge, values)).toEqual({
            drafts: {},
            pending: { "rabbit.viewCharge": 0.06 },
            command: { type: "setSetting", owner: "rabbit", key: "viewCharge", value: 0.06 },
        });
    });

    test("a release on the value last sent, or with no draft, sends nothing", () => {
        const values = { rabbit: { viewCharge: 0.05 } };
        expect(release({ "rabbit.viewCharge": 0.05 }, {}, charge, values)).toEqual({
            drafts: {},
            pending: {},
            command: null,
        });
        expect(
            release({ "rabbit.viewCharge": 0.06 }, { "rabbit.viewCharge": 0.06 }, charge, values),
        ).toEqual({
            drafts: {},
            pending: { "rabbit.viewCharge": 0.06 },
            command: null,
        });
        expect(release({}, { "rabbit.viewCharge": 0.06 }, charge, values)).toEqual({
            drafts: {},
            pending: { "rabbit.viewCharge": 0.06 },
            command: null,
        });
    });

    test("dragging back to the snapshot's value before the world shows the last one sends it", () => {
        const values = { rabbit: { viewCharge: 0.05 } };
        expect(
            release({ "rabbit.viewCharge": 0.05 }, { "rabbit.viewCharge": 0.06 }, charge, values),
        ).toEqual({
            drafts: {},
            pending: { "rabbit.viewCharge": 0.05 },
            command: { type: "setSetting", owner: "rabbit", key: "viewCharge", value: 0.05 },
        });
    });

    test("a value sent is let go once the snapshot shows it", () => {
        const pending = { "rabbit.stride": 4, "world.grassPerDrop": 9 };
        expect(
            settle(pending, [stride, perDrop], {
                rabbit: { stride: 4 },
                world: { grassPerDrop: 7 },
            }),
        ).toEqual({
            "world.grassPerDrop": 9,
        });
    });

    test("restoring one names its owner and key, restoring all names nothing", () => {
        expect(restoreOne(stride)).toEqual({
            type: "restoreDefaults",
            owner: "rabbit",
            key: "stride",
        });
        expect(RESTORE_ALL).toEqual({ type: "restoreDefaults" });
    });

    test("a value lands on its step from the minimum, whatever the step's digits", () => {
        expect(onStep(0.060000000000000005, charge)).toBe(0.06);
        expect(onStep(0.6, declaration("a", "quarter", 0, 0.25))).toBe(0.5);
        expect(onStep(6.1, declaration("a", "half", 0, 2.5))).toBe(5);
        expect(onStep(0.36, declaration("a", "offset", 0.05, 0.1, 0.05))).toBe(0.35);
    });

    test("a value shows as many decimals as its step or minimum has", () => {
        expect(formatValue(15, stride)).toBe("15");
        expect(formatValue(0.030000000000000002, charge)).toBe("0.03");
        expect(formatValue(0.5, declaration("a", "quarter", 0, 0.25))).toBe("0.50");
        expect(formatValue(5, declaration("a", "half", 0, 2.5))).toBe("5.0");
    });

    test("a key is dropped from a map of held values", () => {
        expect(
            withoutKey({ "rabbit.stride": 2, "world.grassPerDrop": 3 }, "rabbit.stride"),
        ).toEqual({
            "world.grassPerDrop": 3,
        });
    });
});

const mutation: SettingDeclaration = {
    owner: "world",
    key: "mutation",
    shape: "toggle",
    description: "Mutate.",
    min: 0,
    max: 1,
    step: 1,
    defaultValue: 1,
};

describe("a toggle", () => {
    test("a click sends the opposite of what it shows and holds it", () => {
        const flipped = flip({}, mutation, { world: { mutation: 1 } });
        expect(flipped.command).toEqual({
            type: "setSetting",
            owner: "world",
            key: "mutation",
            value: 0,
        });
        expect(flipped.pending).toEqual({ "world.mutation": 0 });
    });

    test("a second click before the snapshot catches up flips the held value", () => {
        const flipped = flip({ "world.mutation": 0 }, mutation, { world: { mutation: 1 } });
        expect(flipped.command).toEqual({
            type: "setSetting",
            owner: "world",
            key: "mutation",
            value: 1,
        });
        expect(flipped.pending).toEqual({ "world.mutation": 1 });
    });
});
