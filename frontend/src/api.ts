import type {
    CensusRow,
    Command,
    DayReport,
    PlayState,
    WorldSnapshot,
    WorldSummary,
} from "./types";

export class ApiError extends Error {
    constructor(
        readonly status: number,
        message: string,
    ) {
        super(message);
    }
}

export type Api = ReturnType<typeof createApi>;

export function createApi(fetchFn: typeof fetch = (input, init) => fetch(input, init)) {
    async function request(method: string, path: string, body?: unknown): Promise<Response> {
        const response = await fetchFn(path, {
            method,
            headers: body === undefined ? {} : { "Content-Type": "application/json" },
            body: body === undefined ? undefined : JSON.stringify(body),
        });
        if (!response.ok) {
            throw new ApiError(response.status, await errorMessage(response));
        }
        return response;
    }

    async function json<T>(method: string, path: string, body?: unknown): Promise<T> {
        return (await (await request(method, path, body)).json()) as T;
    }

    return {
        listWorlds: () => json<WorldSummary[]>("GET", "/worlds"),
        createWorld: (seed: number | null) =>
            json<{ id: string; snapshot: WorldSnapshot }>(
                "POST",
                "/worlds",
                seed === null ? {} : { seed },
            ),
        deleteWorld: async (id: string) => {
            await request("DELETE", world(id));
        },
        command: async (id: string, command: Command): Promise<WorldSnapshot | null> => {
            const response = await request("POST", `${world(id)}/commands`, command);
            return response.status === 202 ? null : ((await response.json()) as WorldSnapshot);
        },
        step: (id: string, days: number) => json<DayReport>("POST", `${world(id)}/step`, { days }),
        play: (id: string) => json<PlayState>("POST", `${world(id)}/play`),
        pause: (id: string) => json<PlayState>("POST", `${world(id)}/pause`),
        rate: (id: string, daysPerSecond: number) =>
            json<PlayState>("PUT", `${world(id)}/rate`, { daysPerSecond }),
        census: (id: string, from: number) =>
            json<CensusRow[]>("GET", `${world(id)}/census?from=${from}`),
        resetCensus: async (id: string) => {
            await request("DELETE", `${world(id)}/census`);
        },
    };
}

function world(id: string): string {
    return `/worlds/${encodeURIComponent(id)}`;
}

async function errorMessage(response: Response): Promise<string> {
    const text = await response.text();
    try {
        const parsed: unknown = JSON.parse(text);
        if (typeof parsed === "object" && parsed !== null && "error" in parsed) {
            return String(parsed.error);
        }
    } catch {
        // Not JSON, so the status is all there is.
    }
    return `the server answered ${response.status}`;
}
