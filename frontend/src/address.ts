export function worldFromAddress(search: string): string | null {
    const id = new URLSearchParams(search).get("world");
    return id === null || id === "" ? null : id;
}

export function addressFor(id: string | null): string {
    return id === null ? "/" : `/?world=${encodeURIComponent(id)}`;
}
