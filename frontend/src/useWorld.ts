import { useEffect, useState } from "react";
import type { Api } from "./api";
import { firstGap, merge, receive, refreshed } from "./census";
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
        // The first day the chart still lacks, asked for again each day until a fetch fills it.
        let wanted: number | null = null;
        let filling = false;

        const setHistory = (rows: CensusRow[]) => {
            history = rows;
            setState((current) => ({ ...current, history: rows }));
        };
        // A fetch that returns after a reset belongs to the history the reset cleared.
        const fill = () => {
            if (wanted === null || filling) {
                return;
            }
            const asked = generation;
            filling = true;
            api.census(id, wanted)
                .then((rows) => {
                    if (!closed && asked === generation) {
                        setHistory(merge(history, rows));
                        wanted = firstGap(history);
                    }
                })
                .catch(() => undefined)
                .finally(() => {
                    filling = false;
                });
        };
        const restart = () => {
            generation += 1;
            wanted = null;
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
                // A statistics reset sent while away never arrived, so a new connection
                // fetches the history afresh.
                snapshot: (snapshot, first) => {
                    see(snapshot);
                    if (!first) {
                        return;
                    }
                    generation += 1;
                    wanted = null;
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
                        wanted = Math.min(wanted ?? received.missingFrom, received.missingFrom);
                    }
                    fill();
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
