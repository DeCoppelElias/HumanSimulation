export const ART_TILE = 16;
const SIDE_COLUMN = 400;
const CHROME_HEIGHT = 180;

/** Whole numbers only, so every art pixel stays square and sharp. */
export function wholeScale(
    width: number,
    height: number,
    windowWidth: number,
    windowHeight: number,
): number {
    const across = (windowWidth - SIDE_COLUMN) / (width * ART_TILE);
    const down = (windowHeight - CHROME_HEIGHT) / (height * ART_TILE);
    return Math.min(4, Math.max(1, Math.floor(Math.min(across, down))));
}
