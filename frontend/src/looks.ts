import { markedPalette, type Marking, type Marks } from "./markings";
import type { Drawn } from "./motion";
import type { Palette } from "./pixels";
import sheet from "./sprites.json";
import { tuftOf } from "./tiles";

/** What a sprite is drawn from, and the key its texture is cached under. */
export type Look = { key: string; grid: readonly string[]; palette: Palette };

type Kind = { palette: Palette; marks: Marks; frames: Partial<Record<string, string[]>> };

const KINDS: Partial<Record<string, Kind>> = { rabbit: sheet.rabbit, wolf: sheet.wolf };
const GROUND_COVER: Partial<Record<string, { tufts: string[][]; palette: Palette }>> = {
    grass: sheet.grass,
};

/** Null for a species with no art. */
export function lookOf(
    placed: Pick<Drawn, "id" | "spriteKey" | "creature" | "frame">,
    chosen: Marking | null,
): Look | null {
    if (!placed.creature) {
        const cover = GROUND_COVER[placed.spriteKey];
        if (cover === undefined) return null;
        const tuft = tuftOf(placed.id) % cover.tufts.length;
        return {
            key: `${placed.spriteKey}/${tuft}`,
            grid: cover.tufts[tuft],
            palette: cover.palette,
        };
    }
    const kind = KINDS[placed.spriteKey];
    if (kind === undefined) return null;
    const frame = kind.frames[placed.frame] !== undefined ? placed.frame : "stand";
    const grid = kind.frames[frame];
    if (grid === undefined) return null;
    const marked = chosen === null ? "plain" : `${chosen.colour}.${chosen.extent}.${chosen.accent}`;
    return {
        key: `${placed.spriteKey}/${frame}/${marked}`,
        grid,
        palette: chosen === null ? kind.palette : markedPalette(kind, chosen),
    };
}
