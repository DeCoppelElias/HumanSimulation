export const STARTING_RABBITS = 10;

export type SeedInput =
    { kind: "clock" } | { kind: "seed"; seed: number } | { kind: "invalid"; reason: string };

export function readSeed(text: string): SeedInput {
    const trimmed = text.trim();
    if (trimmed === "") {
        return { kind: "clock" };
    }
    if (!/^-?\d+$/.test(trimmed)) {
        return { kind: "invalid", reason: "a seed is a whole number" };
    }
    const seed = Number(trimmed);
    if (!Number.isSafeInteger(seed)) {
        return {
            kind: "invalid",
            reason: `a seed is at most ${Number.MAX_SAFE_INTEGER} either way`,
        };
    }
    return { kind: "seed", seed };
}
