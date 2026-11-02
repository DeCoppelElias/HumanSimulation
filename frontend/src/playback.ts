import { transition, type Moment, type Transition } from "./motion";
import { isNewRun } from "./run";
import { drawn, placements } from "./tiles";
import type { EntityView, PlayState, WorldSnapshot } from "./types";

/** What the page remembers from one day to the next. */
export type Playback = {
    snapshot: WorldSnapshot | null;
    moment: Moment | null;
    facing: Map<number, boolean>;
    /** Every creature still drawn, a dying one included while it fades. */
    entities: Map<number, EntityView>;
};
export type Step = {
    state: Playback;
    transition: Transition;
    /** A new run reuses ids across species, so nothing about an id carries over. */
    newRun: boolean;
    /** Ids on neither day, whose cached looks can go. */
    gone: number[];
};

export const START: Playback = {
    snapshot: null,
    moment: null,
    facing: new Map(),
    entities: new Map(),
};

export function advance(state: Playback, next: WorldSnapshot, play: PlayState): Step {
    const newRun = state.snapshot === null || isNewRun(state.snapshot, next);
    const to: Moment = { day: next.day, placed: placements(next), drawn: drawn(next) };
    const from: Moment =
        !newRun && state.moment !== null
            ? state.moment
            : { day: next.day, placed: new Map(), drawn: new Set() };
    const day = transition(from, to, play, newRun ? new Map() : state.facing);
    const entities = new Map(newRun ? [] : state.entities);
    for (const tile of next.tiles) {
        for (const entity of tile.entities) entities.set(entity.id, entity);
    }
    const gone: number[] = [];
    for (const id of entities.keys()) {
        if (!from.placed.has(id) && !to.placed.has(id)) {
            entities.delete(id);
            gone.push(id);
        }
    }
    return {
        state: { snapshot: next, moment: to, facing: day.facing, entities },
        transition: day,
        newRun,
        gone,
    };
}
