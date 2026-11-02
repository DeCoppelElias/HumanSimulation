import type { Palette } from "./pixels";
import type { GeneRange } from "./types";

export type Marking = { colour: number; extent: number; accent: number };
/** One marking colour: a tint for the whole coat, and the colours the regions it marks take. */
export type Shade = { base: Palette; marked: Palette };
export type Marks = {
    colours: readonly Shade[];
    extents: readonly (readonly string[])[];
    accents: readonly (readonly string[])[];
};

const GAIN = 3;
const COLOURS = 6;
const EXTENTS = 4;
const ACCENTS = 3;

/** A fixed draw from a unit normal per species, channel and gene, so a page load never reshuffles coats. */
export function weight(species: string, channel: string, gene: string): number {
    const next = mulberry32(fnv1a(`${species}:${channel}:${gene}`));
    const u1 = 1 - next();
    const u2 = next();
    return Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
}

/** 0.5 at the species' baseline, toward 0 or 1 as the genome moves along the channel's weights. */
export function blend(
    species: string,
    channel: string,
    genes: Readonly<Record<string, number>>,
    ranges: readonly GeneRange[],
): number {
    const used = ranges.filter((range) => range.max > range.min);
    if (used.length === 0) {
        return 0.5;
    }
    let sum = 0;
    for (const range of used) {
        const value = genes[range.key] ?? range.baseline;
        sum +=
            (weight(species, channel, range.key) * (value - range.baseline)) /
            (range.max - range.min);
    }
    return 0.5 + 0.5 * Math.tanh((GAIN * sum) / Math.sqrt(used.length));
}

export function marking(
    species: string,
    genes: Readonly<Record<string, number>>,
    ranges: readonly GeneRange[],
): Marking {
    const pick = (channel: string, options: number) =>
        Math.min(options - 1, Math.floor(blend(species, channel, genes, ranges) * options));
    return {
        colour: pick("colour", COLOURS),
        extent: pick("extent", EXTENTS),
        accent: pick("accent", ACCENTS),
    };
}

export function markedPalette(kind: { palette: Palette; marks: Marks }, chosen: Marking): Palette {
    const shade = kind.marks.colours[chosen.colour];
    const painted: Record<string, string> = { ...kind.palette, ...shade.base };
    for (const region of [
        ...kind.marks.extents[chosen.extent],
        ...kind.marks.accents[chosen.accent],
    ]) {
        painted[region] = shade.marked[region];
    }
    return painted;
}

function fnv1a(text: string): number {
    let hash = 0x811c9dc5;
    for (let i = 0; i < text.length; i++) {
        hash ^= text.charCodeAt(i);
        hash = Math.imul(hash, 0x01000193);
    }
    return hash >>> 0;
}

function mulberry32(seed: number): () => number {
    let state = seed;
    return () => {
        state = (state + 0x6d2b79f5) | 0;
        let t = Math.imul(state ^ (state >>> 15), 1 | state);
        t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
        return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
}
