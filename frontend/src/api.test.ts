import { describe, expect, test } from "vitest";
import { ApiError, createApi } from "./api";

type Call = { url: string; init: RequestInit | undefined };

function fakeFetch(response: Response, calls: Call[]): typeof fetch {
    return async (input, init) => {
        calls.push({ url: String(input), init });
        return response;
    };
}

describe("the API client", () => {
    test("sends a command as JSON and returns the snapshot", async () => {
        const calls: Call[] = [];
        const snapshot = { seed: 1, day: 0, width: 2, height: 2, tiles: [] };
        const api = createApi(fakeFetch(Response.json(snapshot), calls));
        const result = await api.command("w1", { type: "spawn", species: "rabbit", count: 3 });
        expect(result).toEqual(snapshot);
        expect(calls[0].url).toBe("/worlds/w1/commands");
        expect(calls[0].init?.method).toBe("POST");
        expect(calls[0].init?.body).toBe('{"type":"spawn","species":"rabbit","count":3}');
    });

    test("a command accepted for the next day has no snapshot", async () => {
        const api = createApi(fakeFetch(new Response(null, { status: 202 }), []));
        expect(await api.command("w1", { type: "reset" })).toBeNull();
    });

    test("an error carries the server's message and status", async () => {
        const api = createApi(
            fakeFetch(
                Response.json({ error: "pause the world before stepping it" }, { status: 409 }),
                [],
            ),
        );
        await expect(api.step("w1", 5)).rejects.toEqual(
            new ApiError(409, "pause the world before stepping it"),
        );
    });

    test("an error without JSON still names its status", async () => {
        const api = createApi(fakeFetch(new Response("boom", { status: 500 }), []));
        await expect(api.listWorlds()).rejects.toMatchObject({ status: 500 });
    });

    test("creating without a seed sends no seed", async () => {
        const calls: Call[] = [];
        const api = createApi(
            fakeFetch(Response.json({ id: "w", snapshot: {} }, { status: 201 }), calls),
        );
        await api.createWorld(null);
        expect(calls[0].init?.body).toBe("{}");
    });

    test("the world's id is encoded in the path", async () => {
        const calls: Call[] = [];
        const api = createApi(fakeFetch(Response.json([]), calls));
        await api.census("a/b c", 3);
        expect(calls[0].url).toBe("/worlds/a%2Fb%20c/census?from=3");
    });
});
