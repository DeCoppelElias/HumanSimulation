/** 3 by 5 digits, `w` lit. */
const DIGITS: Record<string, string[]> = {
    "0": ["www", "w.w", "w.w", "w.w", "www"],
    "1": [".w.", "ww.", ".w.", ".w.", "www"],
    "2": ["www", "..w", "www", "w..", "www"],
    "3": ["www", "..w", ".ww", "..w", "www"],
    "4": ["w.w", "w.w", "www", "..w", "..w"],
    "5": ["www", "w..", "www", "..w", "www"],
    "6": ["www", "w..", "www", "w.w", "www"],
    "7": ["www", "..w", ".w.", ".w.", ".w."],
    "8": ["www", "w.w", "www", "w.w", "www"],
    "9": ["www", "w.w", "www", "..w", "www"],
};

/** The chip for a tile holding count creatures, as a grid of `o` ink and `w` white, or null below two. */
export function chip(count: number): string[] | null {
    if (count < 2) {
        return null;
    }
    const digits = [...String(count)].map((digit) => DIGITS[digit]);
    const rows = Array.from(
        { length: 5 },
        (_, y) => "o" + digits.map((digit) => digit[y].replaceAll(".", "o")).join("o") + "o",
    );
    const edge = "o".repeat(rows[0].length);
    return [edge, ...rows, edge];
}
