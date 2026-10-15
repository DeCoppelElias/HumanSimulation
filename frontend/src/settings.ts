import type { Command, SettingDeclaration, Settings } from "./types";

export type SettingGroup = { owner: string; label: string; settings: SettingDeclaration[] };
export type Drafts = Partial<Record<string, number>>;

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

export function draftKey(declaration: SettingDeclaration): string {
    return `${declaration.owner}.${declaration.key}`;
}

export function currentValue(declaration: SettingDeclaration, values: Settings): number {
    return values[declaration.owner]?.[declaration.key] ?? declaration.defaultValue;
}

export function isEdited(declaration: SettingDeclaration, values: Settings): boolean {
    return currentValue(declaration, values) !== declaration.defaultValue;
}

export function anyEdited(declarations: SettingDeclaration[], values: Settings): boolean {
    return declarations.some((declaration) => isEdited(declaration, values));
}

/** Shows as many decimals as the step has, so 0.03 does not show as 0.030000000000000002. */
export function formatValue(value: number, step: number): string {
    return value.toFixed(Math.max(0, Math.ceil(-Math.log10(step) - 1e-9)));
}

/** A released slider sends one command, rounded to its step, and none when it ends where it began. */
export function commitDraft(
    draft: number | undefined,
    declaration: SettingDeclaration,
    values: Settings,
): Command | null {
    if (draft === undefined) {
        return null;
    }
    const value = Number(formatValue(draft, declaration.step));
    if (value === currentValue(declaration, values)) {
        return null;
    }
    return { type: "setSetting", owner: declaration.owner, key: declaration.key, value };
}

export function restoreOne(declaration: SettingDeclaration): Command {
    return { type: "restoreDefaults", owner: declaration.owner, key: declaration.key };
}

export function withoutDraft(drafts: Drafts, key: string): Drafts {
    return Object.fromEntries(Object.entries(drafts).filter(([held]) => held !== key));
}
