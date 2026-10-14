import { useState } from "react";
import type { Api } from "./api";
import { messageOf } from "./messages";
import { FAST_FORWARD_DEFAULT, FAST_FORWARD_MAX, fastForward } from "./fastForward";
import { readSeed } from "./seed";
import { populate, STARTING_RABBITS } from "./start";
import type { PlayState } from "./types";

type Props = {
    api: Api;
    id: string;
    play: PlayState;
    species: string[];
    spawnAs: string | null;
    onSelect(species: string): void;
    onError(message: string): void;
    onLeave(): void;
};

const RATES = [0.5, 1, 2, 5, 10];

export function Controls({ api, id, play, species, spawnAs, onSelect, onError, onLeave }: Props) {
    const [busy, setBusy] = useState(false);
    const [count, setCount] = useState(String(STARTING_RABBITS));
    const [days, setDays] = useState(String(FAST_FORWARD_DEFAULT));
    const [seedText, setSeedText] = useState("");

    const run = async (action: () => Promise<unknown>) => {
        setBusy(true);
        try {
            await action();
        } catch (error) {
            onError(messageOf(error));
        } finally {
            setBusy(false);
        }
    };

    const reset = () =>
        run(async () => {
            const seed = readSeed(seedText);
            if (seed.kind === "invalid") {
                throw new Error(seed.reason);
            }
            await api.command(
                id,
                seed.kind === "seed" ? { type: "reset", seed: seed.seed } : { type: "reset" },
            );
            await populate(api, id);
        });

    const remove = () =>
        run(async () => {
            await api.deleteWorld(id);
            onLeave();
        });

    return (
        <div className="controls">
            <button disabled={busy || play.playing} onClick={() => void run(() => api.step(id, 1))}>
                Step
            </button>
            <button
                disabled={busy}
                onClick={() => void run(() => (play.playing ? api.pause(id) : api.play(id)))}
            >
                {play.playing ? "Pause" : "Play"}
            </button>
            <label>
                Days a second
                <select
                    disabled={busy}
                    value={play.daysPerSecond}
                    onChange={(e) => void run(() => api.rate(id, Number(e.target.value)))}
                >
                    {(RATES.includes(play.daysPerSecond)
                        ? RATES
                        : [...RATES, play.daysPerSecond].sort((a, b) => a - b)
                    ).map((rate) => (
                        <option key={rate} value={rate}>
                            {rate}
                        </option>
                    ))}
                </select>
            </label>
            <label>
                Fast-forward
                <input
                    type="number"
                    min={1}
                    max={FAST_FORWARD_MAX}
                    value={days}
                    onChange={(e) => setDays(e.target.value)}
                />
            </label>
            <button
                disabled={busy}
                onClick={() => void run(() => fastForward(api, id, Number(days), play.playing))}
            >
                Go
            </button>
            <label>
                Spawn
                <select
                    disabled={busy}
                    value={spawnAs ?? ""}
                    onChange={(e) => onSelect(e.target.value)}
                >
                    {species.map((name) => (
                        <option key={name} value={name}>
                            {name}
                        </option>
                    ))}
                </select>
            </label>
            <label>
                Count
                <input
                    type="number"
                    min={1}
                    value={count}
                    onChange={(e) => setCount(e.target.value)}
                />
            </label>
            <button
                disabled={busy || spawnAs === null}
                onClick={() => {
                    if (spawnAs !== null) {
                        void run(() =>
                            api.command(id, {
                                type: "spawn",
                                species: spawnAs,
                                count: Number(count),
                            }),
                        );
                    }
                }}
            >
                Spawn
            </button>
            <label>
                Seed{" "}
                <input
                    value={seedText}
                    placeholder="from the clock"
                    onChange={(e) => setSeedText(e.target.value)}
                />
            </label>
            <button disabled={busy} onClick={() => void reset()}>
                Reset world
            </button>
            <button disabled={busy} onClick={() => void run(() => api.resetCensus(id))}>
                Reset statistics
            </button>
            <button onClick={onLeave}>Worlds</button>
            <button disabled={busy} onClick={() => void remove()}>
                Delete world
            </button>
        </div>
    );
}
