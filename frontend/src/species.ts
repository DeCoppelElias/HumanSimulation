const PREFERRED = "rabbit";

export function defaultSpecies(known: string[]): string | null {
    return known.includes(PREFERRED) ? PREFERRED : (known[0] ?? null);
}
