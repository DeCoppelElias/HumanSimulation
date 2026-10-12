import type { DayReport, PlayState, WorldSnapshot } from "./types";

export type StreamHandlers = {
    /** `first` marks the snapshot a connection opens with, after which anything sent while away is lost. */
    snapshot(snapshot: WorldSnapshot, first: boolean): void;
    day(report: DayReport): void;
    playState(state: PlayState): void;
    censusReset(): void;
    gone(): void;
};

export type EventSourceLike = {
    readonly readyState: number;
    onerror: ((event: Event) => void) | null;
    addEventListener(type: string, listener: (event: MessageEvent<string>) => void): void;
    close(): void;
};

export type Visibility = {
    hidden(): boolean;
    onChange(listener: () => void): () => void;
};

const CLOSED = 2;

/** A hidden tab holds no connection, since a browser allows six per origin across every tab. */
export function openWorldStream(
    id: string,
    handlers: StreamHandlers,
    createSource: (url: string) => EventSourceLike,
    visibility: Visibility,
): () => void {
    let source: EventSourceLike | null = null;

    const connect = () => {
        const opened = createSource(`/worlds/${encodeURIComponent(id)}/events`);
        let first = true;
        opened.addEventListener("snapshot", (event) => {
            handlers.snapshot(JSON.parse(event.data).snapshot, first);
            first = false;
        });
        opened.addEventListener("day", (event) => handlers.day(JSON.parse(event.data)));
        opened.addEventListener("playState", (event) => handlers.playState(JSON.parse(event.data)));
        opened.addEventListener("censusReset", () => handlers.censusReset());
        // The browser retries a dropped connection by itself and closes only on an HTTP error, such as a 404.
        opened.onerror = () => {
            first = true;
            if (opened.readyState === CLOSED) {
                handlers.gone();
            }
        };
        source = opened;
    };

    const disconnect = () => {
        source?.close();
        source = null;
    };

    const unsubscribe = visibility.onChange(() => {
        if (visibility.hidden()) {
            disconnect();
        } else if (source === null) {
            connect();
        }
    });
    if (!visibility.hidden()) {
        connect();
    }
    return () => {
        unsubscribe();
        disconnect();
    };
}

export function documentVisibility(): Visibility {
    return {
        hidden: () => document.hidden,
        onChange: (listener) => {
            document.addEventListener("visibilitychange", listener);
            return () => document.removeEventListener("visibilitychange", listener);
        },
    };
}
