import type { DayReport, PlayState } from "./types";

export const FAST_FORWARD_DEFAULT = 100;
export const FAST_FORWARD_MAX = 10_000;

export type Stepper = {
    pause(id: string): Promise<PlayState>;
    play(id: string): Promise<PlayState>;
    step(id: string, days: number): Promise<DayReport>;
};

/** The server refuses to step a playing world, so a playing one is paused around the step. */
export async function fastForward(
    api: Stepper,
    id: string,
    days: number,
    playing: boolean,
): Promise<DayReport> {
    if (!Number.isInteger(days) || days < 1 || days > FAST_FORWARD_MAX) {
        throw new RangeError(`fast-forward between 1 and ${FAST_FORWARD_MAX} days, got ${days}`);
    }
    if (!playing) {
        return api.step(id, days);
    }
    await api.pause(id);
    try {
        return await api.step(id, days);
    } finally {
        await api.play(id);
    }
}
