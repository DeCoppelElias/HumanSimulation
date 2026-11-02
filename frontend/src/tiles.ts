import type { WorldSnapshot } from "./types";

export type Placed = {
    id: number;
    spriteKey: string;
    creature: boolean;
    x: number;
    y: number;
    /** Art pixels from the tile's spot, for a creature drawn in a group. */
    ox: number;
    oy: number;
};
export type Stack = {
    x: number;
    y: number;
    groundId: number | null;
    /** The creatures drawn, lowest ids first. */
    shown: number[];
    creatures: number;
};

const GROUP = 3;
const NUDGES: Partial<Record<number, [number, number][]>> = {
    1: [[0, 0]],
    2: [
        [-3, 0],
        [3, 0],
    ],
    3: [
        [-4, 1],
        [4, 1],
        [0, -2],
    ],
};
/** In front of other creatures on its row; the snapshot does not say who eats whom. */
const LAYERS: Partial<Record<string, number>> = { wolf: 2 };

export function nudges(count: number): [number, number][] {
    return NUDGES[Math.min(GROUP, count)] ?? [];
}

/** Ground cover is drawn under the creatures standing on it, and only creatures are counted. */
export function stacks(snapshot: WorldSnapshot): Stack[] {
    return snapshot.tiles
        .filter((tile) => tile.entities.length > 0)
        .map((tile) => {
            const creatures = tile.entities
                .filter((entity) => entity.creature)
                .map((entity) => entity.id)
                .sort((a, b) => a - b);
            const ground = tile.entities
                .filter((entity) => !entity.creature)
                .map((entity) => entity.id);
            return {
                x: tile.at.x,
                y: tile.at.y,
                groundId: ground.length === 0 ? null : Math.min(...ground),
                shown: creatures.slice(0, GROUP),
                creatures: creatures.length,
            };
        });
}

export function placements(snapshot: WorldSnapshot): Map<number, Placed> {
    const offsets = new Map<number, [number, number]>();
    for (const stack of stacks(snapshot)) {
        const spots = nudges(stack.shown.length);
        stack.shown.forEach((id, i) => offsets.set(id, spots[i]));
    }
    const placed = new Map<number, Placed>();
    for (const tile of snapshot.tiles) {
        for (const entity of tile.entities) {
            const [ox, oy] = offsets.get(entity.id) ?? [0, 0];
            placed.set(entity.id, {
                id: entity.id,
                spriteKey: entity.spriteKey,
                creature: entity.creature,
                x: tile.at.x,
                y: tile.at.y,
                ox,
                oy,
            });
        }
    }
    return placed;
}

export function drawn(snapshot: WorldSnapshot): Set<number> {
    const ids = new Set<number>();
    for (const stack of stacks(snapshot)) {
        if (stack.groundId !== null) ids.add(stack.groundId);
        stack.shown.forEach((id) => ids.add(id));
    }
    return ids;
}

/** Larger draws in front: rows first, then layer, then the nudge within a tile. */
export function depth(placed: Pick<Placed, "spriteKey" | "y" | "oy">): number {
    return placed.y * 1000 + (LAYERS[placed.spriteKey] ?? 1) * 100 + placed.oy;
}

export function groundVariant(x: number, y: number): number {
    let hash = Math.imul(x, 73856093) ^ Math.imul(y, 19349663);
    hash = Math.imul(hash ^ (hash >>> 13), 0x5bd1e995);
    return ((hash ^ (hash >>> 15)) >>> 0) % 4;
}

export function tuftOf(id: number): number {
    return id % 3;
}
