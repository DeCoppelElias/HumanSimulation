import { useEffect } from "react";
import type { Api } from "./api";
import { messageOf } from "./messages";
import { Chart } from "./Chart";
import { Controls } from "./Controls";
import { Grid } from "./Grid";
import { useWorld } from "./useWorld";

type Props = {
    api: Api;
    id: string;
    onLeave(notice: string | null): void;
    onError(message: string): void;
};

export function WorldView({ api, id, onLeave, onError }: Props) {
    const { snapshot, play, history, gone } = useWorld(api, id);

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
                onTileClick={(at) =>
                    void api
                        .command(id, { type: "spawnAt", species: "rabbit", at })
                        .catch((error: unknown) => onError(messageOf(error)))
                }
                onError={onError}
            />
            <Chart history={history} />
            <Controls
                api={api}
                id={id}
                play={play}
                onError={onError}
                onLeave={() => onLeave(null)}
            />
        </section>
    );
}
