export type GridPosition = { x: number; y: number };
export type EntityView = {
    id: number;
    species: string;
    spriteKey: string;
    creature: boolean;
    info: Record<string, number>;
};
export type TileView = { at: GridPosition; fields: Record<string, number>; entities: EntityView[] };
/** Current setting values by owner, then by key. */
export type Settings = Partial<Record<string, Partial<Record<string, number>>>>;
export type SettingDeclaration = {
    owner: string;
    key: string;
    description: string;
    min: number;
    max: number;
    step: number;
    defaultValue: number;
};
export type WorldSnapshot = {
    seed: number;
    day: number;
    width: number;
    height: number;
    species: string[];
    tiles: TileView[];
    settings: Settings;
};
export type CensusRow = {
    day: number;
    population: Record<string, number>;
    geneMeans: Record<string, Record<string, number>>;
};
export type DayReport = { snapshot: WorldSnapshot; census: CensusRow };
export type PlayState = { playing: boolean; daysPerSecond: number };
export type WorldSummary = {
    id: string;
    width: number;
    height: number;
    seed: number;
    day: number;
    playing: boolean;
    population: Record<string, number>;
};
export type Command =
    | { type: "spawn"; species: string; count: number }
    | { type: "spawnAt"; species: string; at: GridPosition }
    | { type: "reset"; seed?: number }
    | { type: "setSetting"; owner: string; key: string; value: number }
    | { type: "restoreDefaults"; owner?: string; key?: string };
