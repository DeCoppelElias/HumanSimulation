import type { Command, SettingDeclaration, Settings } from "./types";

export type SettingGroup = { owner: string; label: string; settings: SettingDeclaration[] };
/** Values the page holds by owner and key: drafts not yet sent, or values sent and not yet in a snapshot. */
export type Held = Partial<Record<string, number>>;
export type Release = { drafts: Held; pending: Held; command: Command | null };

export const RESTORE_ALL: Command = { type: "restoreDefaults" };

/** Declarations arrive in panel order, the world first, so each group keeps the place of its first setting. */
export function groups(declarations: SettingDeclaration[]): SettingGroup[] {
    const byOwner = new Map<string, SettingDeclaration[]>();
    for (const declaration of declarations) {
        byOwner.set(declaration.owner, [...(byOwner.get(declaration.owner) ?? []), declaration]);
    }
    return [...byOwner].map(([owner, settings]) => ({
        owner,
        label: owner.charAt(0).toUpperCase() + owner.slice(1),
        settings,
    }));
}

export function heldKey(declaration: SettingDeclaration): string {
    return `${declaration.owner}.${declaration.key}`;
}

export function currentValue(declaration: SettingDeclaration, values: Settings): number {
    return values[declaration.owner]?.[declaration.key] ?? declaration.defaultValue;
}

/** A slider being dragged shows its draft, one just released shows what it sent until the snapshot catches up. */
export function shownValue(
    drafts: Held,
    pending: Held,
    declaration: SettingDeclaration,
    values: Settings,
): number {
    const key = heldKey(declaration);
    return drafts[key] ?? pending[key] ?? currentValue(declaration, values);
}

export function isEdited(declaration: SettingDeclaration, values: Settings): boolean {
    return currentValue(declaration, values) !== declaration.defaultValue;
}

export function anyEdited(declarations: SettingDeclaration[], values: Settings): boolean {
    return declarations.some((declaration) => isEdited(declaration, values));
}

/** Counted from the number's own digits, so 0.25 has two and 2.5 has one. */
function decimalsOf(x: number): number {
    const [mantissa, exponent = "0"] = String(x).split("e");
    const dot = mantissa.indexOf(".");
    const fraction = dot < 0 ? 0 : mantissa.length - dot - 1;
    return Math.max(0, fraction - Number(exponent));
}

function decimals(declaration: SettingDeclaration): number {
    return Math.max(decimalsOf(declaration.step), decimalsOf(declaration.min));
}

/** The nearest value on the step counted from the minimum, as the server stores it. */
export function onStep(value: number, declaration: SettingDeclaration): number {
    const steps = Math.round((value - declaration.min) / declaration.step);
    return Number((declaration.min + steps * declaration.step).toFixed(decimals(declaration)));
}

export function formatValue(value: number, declaration: SettingDeclaration): string {
    return value.toFixed(decimals(declaration));
}

/** A released slider sends its draft once, on its step, and holds it as pending; nothing when it ends on the value last sent. */
export function release(
    drafts: Held,
    pending: Held,
    declaration: SettingDeclaration,
    values: Settings,
): Release {
    const key = heldKey(declaration);
    const draft = drafts[key];
    const remaining = withoutKey(drafts, key);
    if (draft === undefined) {
        return { drafts: remaining, pending, command: null };
    }
    const value = onStep(draft, declaration);
    if (value === (pending[key] ?? currentValue(declaration, values))) {
        return { drafts: remaining, pending, command: null };
    }
    return {
        drafts: remaining,
        pending: { ...pending, [key]: value },
        command: { type: "setSetting", owner: declaration.owner, key: declaration.key, value },
    };
}

/** Lets go of every value sent that the snapshot now shows. */
export function settle(pending: Held, declarations: SettingDeclaration[], values: Settings): Held {
    return Object.fromEntries(
        Object.entries(pending).filter(([key, value]) => {
            const declaration = declarations.find((one) => heldKey(one) === key);
            return declaration === undefined || currentValue(declaration, values) !== value;
        }),
    );
}

export function restoreOne(declaration: SettingDeclaration): Command {
    return { type: "restoreDefaults", owner: declaration.owner, key: declaration.key };
}

export function withoutKey(held: Held, key: string): Held {
    return Object.fromEntries(Object.entries(held).filter(([one]) => one !== key));
}
