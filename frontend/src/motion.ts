import type { Placed } from "./tiles";
import type { PlayState } from "./types";

export const PAUSED_GLIDE_MS = 300;
export const FADE_MS = 300;

export type Timing = { glideMs: number; fadeMs: number };
export type Transition = {
    from: Map<number, Placed>;
    to: Map<number, Placed>;
    fromDrawn: Set<number>;
    toDrawn: Set<number>;
    timing: Timing;
};
export type Sprite = Placed & { alpha: number };

/** Only consecutive days glide: across a gap the straight line is a path nobody walked. */
export function timing(dayDelta: number, play: PlayState): Timing {
    if (dayDelta !== 1) {
        return { glideMs: 0, fadeMs: FADE_MS };
    }
    const glideMs = play.playing ? 1000 / play.daysPerSecond : PAUSED_GLIDE_MS;
    return { glideMs, fadeMs: glideMs };
}

export function frameAt(transition: Transition, elapsedMs: number): Sprite[] {
    const glide = progress(elapsedMs, transition.timing.glideMs);
    const fade = progress(elapsedMs, transition.timing.fadeMs);
    const sprites: Sprite[] = [];
    for (const [id, end] of transition.to) {
        const start = transition.from.get(id);
        if (start === undefined) {
            if (transition.toDrawn.has(id)) {
                sprites.push({ ...end, alpha: fade });
            }
            continue;
        }
        const covering = transition.fromDrawn.has(id) && glide < 1;
        if (transition.toDrawn.has(id) || covering) {
            sprites.push({
                ...end,
                x: lerp(start.x, end.x, glide),
                y: lerp(start.y, end.y, glide),
                alpha: 1,
            });
        }
    }
    for (const [id, start] of transition.from) {
        if (!transition.to.has(id) && transition.fromDrawn.has(id) && fade < 1) {
            sprites.push({ ...start, alpha: 1 - fade });
        }
    }
    return sprites.sort((a, b) => a.id - b.id);
}

export function finished(transition: Transition, elapsedMs: number): boolean {
    return elapsedMs >= Math.max(transition.timing.glideMs, transition.timing.fadeMs);
}

function progress(elapsedMs: number, durationMs: number): number {
    return durationMs <= 0 ? 1 : Math.min(1, Math.max(0, elapsedMs / durationMs));
}

function lerp(from: number, to: number, amount: number): number {
    return from + (to - from) * amount;
}
