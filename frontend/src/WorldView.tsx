import { useEffect, useState } from "react";
import type { Api } from "./api";
import { messageOf } from "./messages";
import { defaultSpecies } from "./species";
import { Chart } from "./Chart";
import { Controls } from "./Controls";
import { CreatureTable } from "./CreatureTable";
import { Grid } from "./Grid";
import { afterClick, inspectTile, resolve, sightOf, type Picked } from "./inspect";
import { Inspector } from "./Inspector";
import { isNewRun } from "./run";
import { SettingsPanel } from "./SettingsPanel";
import type { EntityView, WorldSnapshot } from "./types";
import { useWorld } from "./useWorld";

type Props = {
    api: Api;
    id: string;
    onLeave(notice: string | null): void;
    onError(message: string): void;
};

export function WorldView({ api, id, onLeave, onError }: Props) {
    const { snapshot, play, history, gone } = useWorld(api, id);
    const [selected, setSelected] = useState<string | null>(null);
    const [mode, setMode] = useState<"inspect" | "spawn">("inspect");
    const [picked, setPicked] = useState<Picked | null>(null);
    const [tile, setTile] = useState<EntityView[]>([]);
    const [run, setRun] = useState(0);
    const [seen, setSeen] = useState<WorldSnapshot | null>(null);
    // A new run reuses ids, so whatever was picked belonged to the old one.
    if (snapshot !== null && snapshot !== seen) {
        if (seen !== null && isNewRun(seen, snapshot)) {
            setPicked(null);
            setTile([]);
            setRun((count) => count + 1);
        }
        setSeen(snapshot);
    }
    const species = snapshot?.species ?? [];
    const spawnAs =
        selected !== null && species.includes(selected) ? selected : defaultSpecies(species);

    useEffect(() => {
        if (gone) {
            onLeave("That world is not on this server. Pick or create one.");
        }
    }, [gone, onLeave]);

    if (snapshot === null) {
        return <p>Connecting…</p>;
    }
    const resolved = resolve(snapshot, picked);
    return (
        <section className="world">
            <div className="status">
                Seed {snapshot.seed}, day {snapshot.day}
                <span className="click-mode">
                    Click a tile to:
                    <label>
                        <input
                            type="radio"
                            name="click-mode"
                            checked={mode === "inspect"}
                            onChange={() => setMode("inspect")}
                        />
                        inspect
                    </label>
                    <label>
                        <input
                            type="radio"
                            name="click-mode"
                            checked={mode === "spawn"}
                            onChange={() => setMode("spawn")}
                        />
                        spawn
                    </label>
                </span>
            </div>
            <Grid
                snapshot={snapshot}
                play={play}
                sight={sightOf(snapshot, resolved)}
                onTileClick={(at) => {
                    if (mode === "inspect") {
                        const click = inspectTile(snapshot, at);
                        setTile(click.entities);
                        setPicked(afterClick(picked, click));
                    } else if (spawnAs !== null) {
                        void api
                            .command(id, { type: "spawnAt", species: spawnAs, at })
                            .catch((error: unknown) => onError(messageOf(error)));
                    }
                }}
                onError={onError}
            />
            <div className="side">
                <Chart history={history} />
                <Inspector resolved={resolved} tile={tile} onPick={setPicked} />
                <CreatureTable
                    key={run}
                    snapshot={snapshot}
                    selected={picked?.id ?? null}
                    onPick={setPicked}
                />
            </div>
            <SettingsPanel api={api} id={id} values={snapshot.settings} onError={onError} />
            <Controls
                api={api}
                id={id}
                play={play}
                species={species}
                spawnAs={spawnAs}
                onSelect={setSelected}
                onError={onError}
                onLeave={() => onLeave(null)}
            />
        </section>
    );
}
