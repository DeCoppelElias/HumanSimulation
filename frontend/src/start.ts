import type { Api } from "./api";
import type { Command } from "./types";

export const STARTING_RABBITS = 10;
export const STARTING_GRASS = 7;

const STARTING_POPULATION: Command[] = [
    { type: "spawn", species: "rabbit", count: STARTING_RABBITS },
    { type: "spawn", species: "grass", count: STARTING_GRASS },
];

/** A world starts empty, so whoever creates or resets one sends its starting population. */
export async function populate(api: Pick<Api, "command">, id: string): Promise<void> {
    for (const command of STARTING_POPULATION) {
        await api.command(id, command);
    }
}
