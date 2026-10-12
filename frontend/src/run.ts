import type { WorldSnapshot } from "./types";

/** Ids and days repeat after a reset, so diffing across one would glide rabbits that never moved. */
export function isNewRun(previous: WorldSnapshot, next: WorldSnapshot): boolean {
    return previous.seed !== next.seed || next.day < previous.day;
}
