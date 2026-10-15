import { useEffect, useState } from "react";
import type { Api } from "./api";
import { messageOf } from "./messages";
import { defaultSpecies } from "./species";
import { Chart } from "./Chart";
import { Controls } from "./Controls";
import { Grid } from "./Grid";
import { SettingsPanel } from "./SettingsPanel";
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
    return (
        <section className="world">
            <div className="status">
                Seed {snapshot.seed}, day {snapshot.day}
            </div>
            <Grid
                snapshot={snapshot}
                play={play}
                onTileClick={(at) => {
                    if (spawnAs !== null) {
                        void api
                            .command(id, { type: "spawnAt", species: spawnAs, at })
                            .catch((error: unknown) => onError(messageOf(error)));
                    }
                }}
                onError={onError}
            />
            <Chart history={history} />
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
