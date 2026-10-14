import type { WorldSnapshot } from "./types";

export type Placed = { id: number; spriteKey: string; creature: boolean; x: number; y: number };
export type Stack = {
    x: number;
    y: number;
    groundId: number | null;
    topId: number | null;
    creatures: number;
};

export function placements(snapshot: WorldSnapshot): Map<number, Placed> {
    const placed = new Map<number, Placed>();
    for (const tile of snapshot.tiles) {
        for (const entity of tile.entities) {
            placed.set(entity.id, {
                id: entity.id,
                spriteKey: entity.spriteKey,
                creature: entity.creature,
                x: tile.at.x,
                y: tile.at.y,
            });
        }
    }
    return placed;
}

const lowest = (ids: number[]): number | null => (ids.length === 0 ? null : Math.min(...ids));

/** Ground cover is drawn under the creature standing on it, and only creatures are counted. */
export function stacks(snapshot: WorldSnapshot): Stack[] {
    return snapshot.tiles
        .filter((tile) => tile.entities.length > 0)
        .map((tile) => {
            const creatures = tile.entities
                .filter((entity) => entity.creature)
                .map((entity) => entity.id);
            const ground = tile.entities
                .filter((entity) => !entity.creature)
                .map((entity) => entity.id);
            return {
                x: tile.at.x,
                y: tile.at.y,
                groundId: lowest(ground),
                topId: lowest(creatures),
                creatures: creatures.length,
            };
        });
}

export function drawn(snapshot: WorldSnapshot): Set<number> {
    const ids = new Set<number>();
    for (const stack of stacks(snapshot)) {
        if (stack.groundId !== null) ids.add(stack.groundId);
        if (stack.topId !== null) ids.add(stack.topId);
    }
    return ids;
}
