export type Pixels = { width: number; height: number; data: Uint8ClampedArray<ArrayBuffer> };
export type Palette = Readonly<Record<string, string>>;

/** A grid of palette letters as RGBA, with `.` left clear. */
export function rasterize(grid: readonly string[], palette: Palette): Pixels {
    const height = grid.length;
    const width = grid[0]?.length ?? 0;
    const data = new Uint8ClampedArray(width * height * 4);
    grid.forEach((row, y) => {
        if (row.length !== width) {
            throw new Error(`row ${y} is ${row.length} wide, not ${width}`);
        }
        for (let x = 0; x < width; x++) {
            const letter = row[x];
            if (letter === ".") continue;
            const hex = palette[letter];
            if (hex === undefined) {
                throw new Error(`no colour for ${letter} at ${x}, ${y}`);
            }
            const at = (y * width + x) * 4;
            data[at] = parseInt(hex.slice(1, 3), 16);
            data[at + 1] = parseInt(hex.slice(3, 5), 16);
            data[at + 2] = parseInt(hex.slice(5, 7), 16);
            data[at + 3] = 255;
        }
    });
    return { width, height, data };
}
