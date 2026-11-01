import type { Placed } from "./tiles";
import type { PlayState } from "./types";

export const FADE_MS = 300;
const MOVE_MS = 260;
const TILE_MS = 160;
const DAY_SHARE = 0.9;
const GLIDE_BELOW_MS = 150;
const BLINK_MS = 150;

/** How one day's change plays: whether things move, the cap a move gets, and how long fades last. */
export type Timing = { moves: boolean; capMs: number | null; glide: boolean; fadeMs: number };
export type Stride = "bound" | "walk" | "leap" | "glide";
/** A frame, the share of the path done, and the height above the ground in art pixels. */
export type Pose = { frame: string; along: number; lift: number };
export type Moment = { day: number; placed: Map<number, Placed>; drawn: Set<number> };
export type Move = { stride: Stride; tiles: number; ms: number };
export type Transition = {
    from: Moment;
    to: Moment;
    timing: Timing;
    moves: Map<number, Move>;
    /** True where a creature faces left. */
    facing: Map<number, boolean>;
};
export type Drawn = Placed & { frame: string; lift: number; flipped: boolean; alpha: number };

const GAITS: Partial<Record<string, "bound" | "stalk">> = { rabbit: "bound", wolf: "stalk" };

/** Only consecutive days move: across a gap the path is one nobody walked. */
export function timing(dayDelta: number, play: PlayState): Timing {
    const capMs = play.playing ? (DAY_SHARE * 1000) / play.daysPerSecond : null;
    return {
        moves: dayDelta === 1,
        capMs,
        glide: capMs !== null && capMs < GLIDE_BELOW_MS,
        fadeMs: capMs === null ? FADE_MS : Math.min(FADE_MS, capMs),
    };
}

export function moveMs(timing: Timing, tiles: number): number {
    const full = MOVE_MS + TILE_MS * tiles;
    return timing.capMs === null ? full : Math.min(full, timing.capMs);
}

export function strideOf(
    spriteKey: string,
    tiles: number,
    pounce: boolean,
    glide: boolean,
): Stride {
    const gait = GAITS[spriteKey];
    if (glide || gait === undefined) return "glide";
    if (gait === "bound") return "bound";
    return tiles > 1 || pounce ? "leap" : "walk";
}

/** Read off the snapshots until the day's report carries a kill. */
export function pounced(from: Map<number, Placed>, to: Map<number, Placed>, id: number): boolean {
    const end = to.get(id);
    if (end === undefined) return false;
    for (const other of from.values()) {
        if (
            other.creature &&
            other.spriteKey !== end.spriteKey &&
            other.x === end.x &&
            other.y === end.y &&
            !to.has(other.id)
        ) {
            return true;
        }
    }
    return false;
}

/** Where a creature is through its move, t running from 0 to 1. */
export function pose(stride: Stride, tiles: number, t: number): Pose {
    if (t >= 1) return { frame: "stand", along: 1, lift: 0 };
    switch (stride) {
        case "glide":
            return { frame: "stand", along: t, lift: 0 };
        case "walk":
            return {
                frame: Math.floor(t * tiles * 2) % 2 === 0 ? "stride" : "stand",
                along: t,
                lift: 0,
            };
        case "bound": {
            if (t < 0.15) return { frame: "crouch", along: 0, lift: 0 };
            if (t >= 0.85) return { frame: "crouch", along: 1, lift: 0 };
            const along = (t - 0.15) / 0.7;
            return { frame: "air", along, lift: arc(7, tiles, along) };
        }
        case "leap": {
            if (t < 0.15) return { frame: "gather", along: 0, lift: 0 };
            if (t >= 0.85) return { frame: "stand", along: 1, lift: 0 };
            const along = Math.min(1, (t - 0.15) / 0.55);
            const frame = t < 0.3 ? "takeoff" : t < 0.55 ? "flight" : "land";
            return { frame, along, lift: along >= 1 ? 0 : arc(4, tiles, along) };
        }
    }
}

/** True when the sprite faces left. */
export function turned(previous: boolean | undefined, dx: number): boolean {
    if (dx < 0) return true;
    if (dx > 0) return false;
    return previous ?? false;
}

/** Every 3 to 5 seconds, set by the id so neighbours do not blink together. */
export function blinking(id: number, nowMs: number): boolean {
    const period = 3000 + ((id * 977) % 2000);
    const phase = (id * 7919) % period;
    return (nowMs + phase) % period < BLINK_MS;
}

export function transition(
    from: Moment,
    to: Moment,
    play: PlayState,
    facings: Map<number, boolean>,
): Transition {
    const time = timing(to.day - from.day, play);
    const moves = new Map<number, Move>();
    const facing = new Map<number, boolean>();
    for (const [id, end] of to.placed) {
        const start = from.placed.get(id);
        const moving = start !== undefined && time.moves && end.creature;
        facing.set(id, turned(facings.get(id), moving ? end.x - start.x : 0));
        if (!moving) continue;
        const tiles = Math.max(Math.abs(end.x - start.x), Math.abs(end.y - start.y));
        if (tiles === 0 && start.ox === end.ox && start.oy === end.oy) continue;
        const pounce = pounced(from.placed, to.placed, id);
        moves.set(id, {
            stride: strideOf(end.spriteKey, tiles, pounce, time.glide),
            tiles,
            ms: moveMs(time, Math.max(1, tiles)),
        });
    }
    return { from, to, timing: time, moves, facing };
}

export function frameAt(transition: Transition, elapsedMs: number, nowMs: number): Drawn[] {
    const { from, to, moves, facing } = transition;
    const fade = progress(elapsedMs, transition.timing.fadeMs);
    const sprites: Drawn[] = [];
    for (const [id, end] of to.placed) {
        const flipped = facing.get(id) ?? false;
        const start = from.placed.get(id);
        if (start === undefined) {
            if (to.drawn.has(id)) {
                sprites.push({ ...end, frame: "stand", lift: 0, flipped, alpha: fade });
            }
            continue;
        }
        const move = moves.get(id);
        const t = move === undefined ? 1 : progress(elapsedMs, move.ms);
        // A creature moving into a crowd it ends up hidden in stays drawn until it lands.
        const covering = from.drawn.has(id) && t < 1;
        if (!to.drawn.has(id) && !covering) continue;
        const now =
            move === undefined
                ? pose("glide", 0, 1)
                : pose(move.stride, Math.max(1, move.tiles), t);
        const blinks = now.frame === "stand" && end.creature && blinking(id, nowMs);
        sprites.push({
            ...end,
            x: lerp(start.x, end.x, now.along),
            y: lerp(start.y, end.y, now.along),
            ox: lerp(start.ox, end.ox, now.along),
            oy: lerp(start.oy, end.oy, now.along),
            frame: blinks ? "blink" : now.frame,
            lift: now.lift,
            flipped,
            alpha: 1,
        });
    }
    for (const [id, start] of from.placed) {
        if (!to.placed.has(id) && from.drawn.has(id) && fade < 1) {
            sprites.push({
                ...start,
                frame: "stand",
                lift: 0,
                flipped: facing.get(id) ?? false,
                alpha: 1 - fade,
            });
        }
    }
    return sprites.sort((a, b) => a.id - b.id);
}

function arc(height: number, tiles: number, along: number): number {
    return round((height + 2.5 * (tiles - 1)) * Math.sin(Math.PI * along));
}

function round(value: number): number {
    return Math.round(value * 1e9) / 1e9;
}

function progress(elapsedMs: number, durationMs: number): number {
    return durationMs <= 0 ? 1 : Math.min(1, Math.max(0, elapsedMs / durationMs));
}

function lerp(from: number, to: number, amount: number): number {
    return from + (to - from) * amount;
}
