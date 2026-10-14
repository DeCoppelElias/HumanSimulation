import { useCallback, useEffect, useState } from "react";
import type { Api } from "./api";
import { messageOf } from "./messages";
import { readSeed } from "./seed";
import { STARTING_RABBITS } from "./start";
import type { WorldSummary } from "./types";

type Props = { api: Api; onOpen(id: string): void; onError(message: string): void };

export function Picker({ api, onOpen, onError }: Props) {
    const [worlds, setWorlds] = useState<WorldSummary[]>([]);
    const [seedText, setSeedText] = useState("");
    const [creating, setCreating] = useState(false);

    const refresh = useCallback(() => {
        api.listWorlds().then(setWorlds, (error: unknown) => onError(messageOf(error)));
    }, [api, onError]);
    useEffect(refresh, [refresh]);

    const create = async () => {
        const seed = readSeed(seedText);
        if (seed.kind === "invalid") {
            onError(seed.reason);
            return;
        }
        setCreating(true);
        try {
            const created = await api.createWorld(seed.kind === "seed" ? seed.seed : null);
            await api.command(created.id, {
                type: "spawn",
                species: "rabbit",
                count: STARTING_RABBITS,
            });
            onOpen(created.id);
        } catch (error) {
            onError(messageOf(error));
        } finally {
            setCreating(false);
        }
    };

    const remove = async (id: string) => {
        try {
            await api.deleteWorld(id);
            refresh();
        } catch (error) {
            onError(messageOf(error));
        }
    };

    return (
        <section className="picker">
            <h1>Worlds</h1>
            <ul>
                {worlds.map((world) => (
                    <li key={world.id}>
                        <button onClick={() => onOpen(world.id)}>
                            {world.width} by {world.height}, seed {world.seed}, day {world.day},{" "}
                            {Object.entries(world.population)
                                .map(([name, count]) => `${count} ${name}`)
                                .join(", ") || "empty"}
                            {world.playing ? ", playing" : ""}
                        </button>
                        <button onClick={() => void remove(world.id)}>Delete</button>
                    </li>
                ))}
            </ul>
            <label>
                Seed{" "}
                <input
                    value={seedText}
                    placeholder="from the clock"
                    onChange={(e) => setSeedText(e.target.value)}
                />
            </label>
            <button disabled={creating} onClick={() => void create()}>
                Create a world
            </button>
        </section>
    );
}
