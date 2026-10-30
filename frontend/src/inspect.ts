import type { EntityView, GridPosition, WorldSnapshot } from "./types";

export type Picked = { id: number; species: string };
export type Resolved =
    | { kind: "none" }
    | { kind: "alive"; entity: EntityView; at: GridPosition }
    | { kind: "gone"; picked: Picked };

/** Ids never repeat within a run, so a picked id that is missing has died, per 0009. */
export function resolve(snapshot: WorldSnapshot, picked: Picked | null): Resolved {
    if (picked === null) {
        return { kind: "none" };
    }
    for (const tile of snapshot.tiles) {
        const entity = tile.entities.find((candidate) => candidate.id === picked.id);
        if (entity !== undefined) {
            return { kind: "alive", entity, at: tile.at };
        }
    }
    return { kind: "gone", picked };
}

export type TileClick = { entities: EntityView[]; select: Picked | null };

/** One entity is selected at once; more, grass included, are listed to pick from. */
export function inspectTile(snapshot: WorldSnapshot, at: GridPosition): TileClick {
    const tile = snapshot.tiles.find(
        (candidate) => candidate.at.x === at.x && candidate.at.y === at.y,
    );
    const entities = tile?.entities ?? [];
    const only = entities.length === 1 ? entities[0] : undefined;
    return {
        entities,
        select: only === undefined ? null : { id: only.id, species: only.species },
    };
}

/** An empty tile deselects; a crowded one keeps the pick until a row of its list is chosen. */
export function afterClick(previous: Picked | null, click: TileClick): Picked | null {
    return click.entities.length === 0 ? null : (click.select ?? previous);
}

/** Only a creature with a view range has a sight circle to draw. */
export function sightOf(snapshot: WorldSnapshot, resolved: Resolved): GridPosition[] {
    if (resolved.kind !== "alive" || !resolved.entity.creature) {
        return [];
    }
    const viewRange = resolved.entity.genes.viewRange;
    return viewRange === undefined
        ? []
        : sight(resolved.at, viewRange, snapshot.width, snapshot.height);
}

/** The grid's circle: the gene rounded, every tile whose squared distance is within its square. */
export function sight(
    at: GridPosition,
    viewRange: number,
    width: number,
    height: number,
): GridPosition[] {
    const range = Math.round(viewRange);
    const seen: GridPosition[] = [];
    for (let y = Math.max(0, at.y - range); y <= Math.min(height - 1, at.y + range); y++) {
        for (let x = Math.max(0, at.x - range); x <= Math.min(width - 1, at.x + range); x++) {
            if ((x - at.x) ** 2 + (y - at.y) ** 2 <= range * range) {
                seen.push({ x, y });
            }
        }
    }
    return seen;
}

export type Row = {
    id: number;
    species: string;
    age: number;
    reserve: number;
    daysToStarve: number;
};
export type SortKey = "age" | "reserve";

export function creatureRows(snapshot: WorldSnapshot): Row[] {
    return snapshot.tiles.flatMap((tile) =>
        tile.entities
            .filter((entity) => entity.creature)
            .map((entity) => ({
                id: entity.id,
                species: entity.species,
                age: entity.info.age ?? 0,
                reserve: entity.info.reserve ?? 0,
                daysToStarve: entity.info.daysToStarve ?? 0,
            })),
    );
}

export function sortRows(rows: Row[], by: SortKey): Row[] {
    return [...rows].sort((a, b) => b[by] - a[by] || a.id - b.id);
}

const LABELS: Record<string, string> = {
    age: "Age",
    daysToLive: "Days to live",
    reserve: "Reserve",
    daysToStarve: "Days to starve",
    amount: "Food value",
};

export function label(key: string): string {
    return LABELS[key] ?? key;
}

export function formatValue(value: number): string {
    return Number.isInteger(value) ? String(value) : value.toFixed(1);
}

export function goneMessage(picked: Picked): string {
    const name = picked.species.charAt(0).toUpperCase() + picked.species.slice(1);
    return `${name} ${picked.id} is no longer alive.`;
}
