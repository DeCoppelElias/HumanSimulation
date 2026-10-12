import { describe, expect, test } from "vitest";
import {
    openWorldStream,
    type EventSourceLike,
    type StreamHandlers,
    type Visibility,
} from "./stream";

class FakeSource implements EventSourceLike {
    readonly listeners = new Map<string, (event: MessageEvent<string>) => void>();
    onerror: ((event: Event) => void) | null = null;
    readyState = 1;
    closed = false;
    constructor(readonly url: string) {}
    addEventListener(type: string, listener: (event: MessageEvent<string>) => void): void {
        this.listeners.set(type, listener);
    }
    close(): void {
        this.closed = true;
    }
    emit(type: string, data: unknown): void {
        this.listeners.get(type)?.({ data: JSON.stringify(data) } as MessageEvent<string>);
    }
}

class FakeVisibility implements Visibility {
    isHidden = false;
    listener: (() => void) | null = null;
    hidden = () => this.isHidden;
    onChange(listener: () => void): () => void {
        this.listener = listener;
        return () => {
            this.listener = null;
        };
    }
    set(hidden: boolean): void {
        this.isHidden = hidden;
        this.listener?.();
    }
}

function recorder() {
    const seen: string[] = [];
    const handlers: StreamHandlers = {
        snapshot: (snapshot, first) =>
            seen.push(`snapshot ${snapshot.day}${first ? " first" : ""}`),
        day: (report) => seen.push(`day ${report.census.day}`),
        playState: (state) => seen.push(`playing ${state.playing}`),
        censusReset: () => seen.push("censusReset"),
        gone: () => seen.push("gone"),
    };
    return { seen, handlers };
}

function sourcesInto(sources: FakeSource[]): (url: string) => FakeSource {
    return (url) => {
        const source = new FakeSource(url);
        sources.push(source);
        return source;
    };
}

describe("the world's stream", () => {
    test("a visible page connects and hands each event on", () => {
        const sources: FakeSource[] = [];
        const { seen, handlers } = recorder();
        openWorldStream("w1", handlers, sourcesInto(sources), new FakeVisibility());
        expect(sources[0].url).toBe("/worlds/w1/events");
        sources[0].emit("playState", { playing: true, daysPerSecond: 2 });
        sources[0].emit("snapshot", { snapshot: { day: 4 } });
        sources[0].emit("day", { snapshot: { day: 5 }, census: { day: 5, population: {} } });
        sources[0].emit("censusReset", {});
        expect(seen).toEqual(["playing true", "snapshot 4 first", "day 5", "censusReset"]);
    });

    test("only the first snapshot of each connection is marked first", () => {
        const sources: FakeSource[] = [];
        const { seen, handlers } = recorder();
        const visibility = new FakeVisibility();
        openWorldStream("w1", handlers, sourcesInto(sources), visibility);
        sources[0].emit("snapshot", { snapshot: { day: 4 } });
        sources[0].emit("snapshot", { snapshot: { day: 4 } });
        visibility.set(true);
        visibility.set(false);
        sources[1].emit("snapshot", { snapshot: { day: 9 } });
        expect(seen).toEqual(["snapshot 4 first", "snapshot 4", "snapshot 9 first"]);
    });

    test("a connection the browser reopens starts with a first snapshot again", () => {
        const sources: FakeSource[] = [];
        const { seen, handlers } = recorder();
        openWorldStream("w1", handlers, sourcesInto(sources), new FakeVisibility());
        sources[0].emit("snapshot", { snapshot: { day: 4 } });
        sources[0].readyState = 0;
        sources[0].onerror?.(new Event("error"));
        sources[0].readyState = 1;
        sources[0].emit("snapshot", { snapshot: { day: 7 } });
        expect(seen).toEqual(["snapshot 4 first", "snapshot 7 first"]);
    });

    test("the world's id is encoded in the stream's address", () => {
        const sources: FakeSource[] = [];
        openWorldStream("a/b c", recorder().handlers, sourcesInto(sources), new FakeVisibility());
        expect(sources[0].url).toBe("/worlds/a%2Fb%20c/events");
    });

    test("a hidden tab closes its stream and opens a new one when shown", () => {
        const sources: FakeSource[] = [];
        const visibility = new FakeVisibility();
        openWorldStream("w1", recorder().handlers, sourcesInto(sources), visibility);
        visibility.set(true);
        expect(sources[0].closed).toBe(true);
        visibility.set(false);
        expect(sources).toHaveLength(2);
        expect(sources[1].closed).toBe(false);
    });

    test("a page opened hidden waits until shown", () => {
        const sources: FakeSource[] = [];
        const visibility = new FakeVisibility();
        visibility.isHidden = true;
        openWorldStream("w1", recorder().handlers, sourcesInto(sources), visibility);
        expect(sources).toHaveLength(0);
        visibility.set(false);
        expect(sources).toHaveLength(1);
    });

    test("closing stops listening and closes the source", () => {
        const sources: FakeSource[] = [];
        const visibility = new FakeVisibility();
        const close = openWorldStream("w1", recorder().handlers, sourcesInto(sources), visibility);
        close();
        expect(sources[0].closed).toBe(true);
        expect(visibility.listener).toBeNull();
    });

    test("a stream the server refuses means the world is gone", () => {
        const sources: FakeSource[] = [];
        const { seen, handlers } = recorder();
        openWorldStream("w1", handlers, sourcesInto(sources), new FakeVisibility());
        sources[0].readyState = 2;
        sources[0].onerror?.(new Event("error"));
        expect(seen).toEqual(["gone"]);
    });

    test("a dropped connection the browser retries is not gone", () => {
        const sources: FakeSource[] = [];
        const { seen, handlers } = recorder();
        openWorldStream("w1", handlers, sourcesInto(sources), new FakeVisibility());
        sources[0].readyState = 0;
        sources[0].onerror?.(new Event("error"));
        expect(seen).toEqual([]);
    });
});
