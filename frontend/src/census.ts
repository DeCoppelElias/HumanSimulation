import type { CensusRow } from "./types";

export type Received = { history: CensusRow[]; missingFrom: number | null };

export function receive(history: CensusRow[], row: CensusRow): Received {
    const last = history.at(-1);
    if (last === undefined) {
        return { history: [row], missingFrom: row.day > 1 ? 1 : null };
    }
    if (row.day <= last.day) {
        return { history, missingFrom: null };
    }
    return {
        history: [...history, row],
        missingFrom: row.day === last.day + 1 ? null : last.day + 1,
    };
}

/** A fetched row wins over one held, since the server's history is the record. */
export function merge(history: CensusRow[], rows: CensusRow[]): CensusRow[] {
    const byDay = new Map<number, CensusRow>();
    for (const row of [...history, ...rows]) {
        byDay.set(row.day, row);
    }
    return [...byDay.values()].sort((a, b) => a.day - b.day);
}

/**
 * The server held every row up to `day` when it answered, so those replace what the page holds,
 * which may predate a statistics reset it never heard about. Rows newer than `day` streamed in since.
 */
export function refreshed(history: CensusRow[], rows: CensusRow[], day: number): CensusRow[] {
    return merge(
        rows,
        history.filter((row) => row.day > day),
    );
}

export function speciesIn(history: CensusRow[]): string[] {
    return [...new Set(history.flatMap((row) => Object.keys(row.population)))].sort();
}

export function toSeries(history: CensusRow[], species: string[]): number[][] {
    return [
        history.map((row) => row.day),
        ...species.map((name) => history.map((row) => row.population[name] ?? 0)),
    ];
}
