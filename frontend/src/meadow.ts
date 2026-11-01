import type { Pixels } from "./pixels";
import { ART_TILE, HEADROOM } from "./scale";
import sheet from "./sprites.json";
import { groundVariant } from "./tiles";

const SHADES = sheet.ground.shades.length;
/** A patch of lush or dry grass spans about this many art pixels. */
const PATCH = 72;
const HEADROOM_DIM = 0.35;
const BAYER = [
    [0, 8, 2, 10],
    [12, 4, 14, 6],
    [3, 11, 1, 9],
    [15, 7, 13, 5],
];

/** A shade from a fractional one, mixing the two around it in an ordered 4 by 4 pattern. */
export function dithered(shade: number, px: number, py: number): number {
    const low = Math.floor(shade);
    const threshold = (BAYER[py & 3][px & 3] + 0.5) / 16;
    const picked = shade - low > threshold ? low + 1 : low;
    return Math.min(SHADES - 1, Math.max(0, picked));
}

/** The shade of one art pixel: slow patches of lush, gold and straw grass, the same every time. */
export function shadeAt(px: number, py: number): number {
    const patches = 0.8 * noise(px / PATCH, py / PATCH) + 0.2 * noise(px / 37 + 7, py / 37 + 3);
    return dithered(0.4 + patches * (SHADES - 1.2), px, py);
}

/** The ground for a world, plus a tile of dimmed headroom above it. */
export function meadow(width: number, height: number): Pixels {
    const widthPx = width * ART_TILE;
    const heightPx = height * ART_TILE;
    const totalHeight = heightPx + HEADROOM;
    const data = new Uint8ClampedArray(new ArrayBuffer(widthPx * totalHeight * 4));
    const shades = sheet.ground.shades.map((shade) =>
        Object.fromEntries(Object.entries(shade).map(([letter, hex]) => [letter, rgb(hex)])),
    );
    for (let y = 0; y < totalHeight; y++) {
        const worldY = y - HEADROOM;
        const tileY = Math.floor(worldY / ART_TILE);
        for (let x = 0; x < widthPx; x++) {
            const tileX = Math.floor(x / ART_TILE);
            const grid = sheet.ground.tiles[groundVariant(tileX, tileY)];
            const letter = grid[((worldY % ART_TILE) + ART_TILE) % ART_TILE][x % ART_TILE];
            const shade = shadeAt(x, Math.max(0, worldY));
            const colour = shades[shade][letter];
            const dim = worldY < 0 ? 1 - HEADROOM_DIM : 1;
            const at = (y * widthPx + x) * 4;
            data[at] = colour[0] * dim;
            data[at + 1] = colour[1] * dim;
            data[at + 2] = colour[2] * dim;
            data[at + 3] = 255;
        }
    }
    return { width: widthPx, height: totalHeight, data };
}

function rgb(hex: string): [number, number, number] {
    return [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16)) as [number, number, number];
}

/** Smooth value noise in 0 to 1 over a lattice of fixed random heights. */
function noise(x: number, y: number): number {
    const x0 = Math.floor(x);
    const y0 = Math.floor(y);
    const sx = smooth(x - x0);
    const sy = smooth(y - y0);
    const top = lerp(lattice(x0, y0), lattice(x0 + 1, y0), sx);
    const bottom = lerp(lattice(x0, y0 + 1), lattice(x0 + 1, y0 + 1), sx);
    return lerp(top, bottom, sy);
}

function lattice(x: number, y: number): number {
    let hash = Math.imul(x, 374761393) ^ Math.imul(y, 668265263);
    hash = Math.imul(hash ^ (hash >>> 13), 1274126177);
    return ((hash ^ (hash >>> 16)) >>> 0) / 4294967296;
}

function smooth(t: number): number {
    return t * t * (3 - 2 * t);
}

function lerp(from: number, to: number, amount: number): number {
    return from + (to - from) * amount;
}
