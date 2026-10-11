import { useEffect, useState } from "react";
import type { Api } from "./api";
import { merge, receive, refreshed } from "./census";
import { isNewRun } from "./run";
import { documentVisibility, openWorldStream } from "./stream";
import type { CensusRow, PlayState, WorldSnapshot } from "./types";

export type WorldState = {
    snapshot: WorldSnapshot | null;
    play: PlayState;
    history: CensusRow[];
    gone: boolean;
};

const INITIAL: WorldState = {
    snapshot: null,
    play: { playing: false, daysPerSecond: 2 },
    history: [],
    gone: false,
};

export function useWorld(api: Api, id: string): WorldState {
    const [state, setState] = useState<WorldState>(INITIAL);

    useEffect(() => {
        let history: CensusRow[] = [];
        let last: WorldSnapshot | null = null;
        let generation = 0;
        let closed = false;

        const setHistory = (rows: CensusRow[]) => {
            history = rows;
            setState((current) => ({ ...current, history: rows }));
        };
        // A fetch that returns after a reset belongs to the history the reset cleared.
        const fill = (from: number) => {
            const asked = generation;
            api.census(id, from)
                .then((rows) => {
                    if (!closed && asked === generation) {
                        setHistory(merge(history, rows));
                    }
                })
                .catch(() => undefined);
        };
        const restart = () => {
            generation += 1;
            setHistory([]);
        };
        const see = (next: WorldSnapshot) => {
            if (last !== null && isNewRun(last, next)) {
                restart();
            }
            last = next;
            setState((current) => ({ ...current, snapshot: next }));
        };

        const close = openWorldStream(
            id,
            {
                // Each new stream starts with a snapshot, and a statistics reset sent while
                // the tab was hidden never arrived, so the history is fetched afresh.
                snapshot: (snapshot) => {
                    see(snapshot);
                    generation += 1;
                    const asked = generation;
                    api.census(id, 1)
                        .then((rows) => {
                            if (!closed && asked === generation) {
                                setHistory(refreshed(history, rows, snapshot.day));
                            }
                        })
                        .catch(() => undefined);
                },
                day: (report) => {
                    see(report.snapshot);
                    const received = receive(history, report.census);
                    setHistory(received.history);
                    if (received.missingFrom !== null) {
                        fill(received.missingFrom);
                    }
                },
                playState: (play) => setState((current) => ({ ...current, play })),
                censusReset: restart,
                gone: () => setState((current) => ({ ...current, gone: true })),
            },
            (url) => new EventSource(url),
            documentVisibility(),
        );
        return () => {
            closed = true;
            close();
        };
    }, [api, id]);

    return state;
}
