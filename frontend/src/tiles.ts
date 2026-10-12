import type { WorldSnapshot } from "./types";

export type Placed = { id: number; spriteKey: string; x: number; y: number };
export type Stack = { x: number; y: number; topId: number; count: number };

export function placements(snapshot: WorldSnapshot): Map<number, Placed> {
    const placed = new Map<number, Placed>();
    for (const tile of snapshot.tiles) {
        for (const entity of tile.entities) {
            placed.set(entity.id, {
                id: entity.id,
                spriteKey: entity.spriteKey,
                x: tile.at.x,
                y: tile.at.y,
            });
        }
    }
    return placed;
}

export function stacks(snapshot: WorldSnapshot): Stack[] {
    return snapshot.tiles
        .filter((tile) => tile.entities.length > 0)
        .map((tile) => ({
            x: tile.at.x,
            y: tile.at.y,
            topId: Math.min(...tile.entities.map((entity) => entity.id)),
            count: tile.entities.length,
        }));
}

export function drawn(snapshot: WorldSnapshot): Set<number> {
    return new Set(stacks(snapshot).map((stack) => stack.topId));
}
