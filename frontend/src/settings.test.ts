import { describe, expect, test } from "vitest";
import {
    RESTORE_ALL,
    anyEdited,
    commitDraft,
    currentValue,
    formatValue,
    groups,
    isEdited,
    restoreOne,
    withoutDraft,
} from "./settings";
import type { SettingDeclaration } from "./types";

const declaration = (
    owner: string,
    key: string,
    defaultValue = 1,
    step = 1,
): SettingDeclaration => ({
    owner,
    key,
    description: `${key}.`,
    min: 0,
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

    test("a released slider sends its value rounded to its step, and nothing when unchanged", () => {
        const values = { rabbit: { viewCharge: 0.03 } };
        expect(commitDraft(0.060000000000000005, charge, values)).toEqual({
            type: "setSetting",
            owner: "rabbit",
            key: "viewCharge",
            value: 0.06,
        });
        expect(commitDraft(0.03, charge, values)).toBeNull();
        expect(commitDraft(undefined, charge, values)).toBeNull();
    });

    test("restoring one names its owner and key, restoring all names nothing", () => {
        expect(restoreOne(stride)).toEqual({
            type: "restoreDefaults",
            owner: "rabbit",
            key: "stride",
        });
        expect(RESTORE_ALL).toEqual({ type: "restoreDefaults" });
    });

    test("a value shows as many decimals as its step", () => {
        expect(formatValue(15, 1)).toBe("15");
        expect(formatValue(0.030000000000000002, 0.01)).toBe("0.03");
        expect(formatValue(0.1, 0.05)).toBe("0.10");
    });

    test("a committed draft is dropped", () => {
        expect(
            withoutDraft({ "rabbit.stride": 2, "world.grassPerDrop": 3 }, "rabbit.stride"),
        ).toEqual({ "world.grassPerDrop": 3 });
    });
});
