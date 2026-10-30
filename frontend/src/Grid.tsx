import { useEffect, useRef } from "react";
import { messageOf } from "./messages";
import { Scene } from "./scene";
import type { GridPosition, PlayState, WorldSnapshot } from "./types";

type Props = {
    snapshot: WorldSnapshot;
    play: PlayState;
    sight: GridPosition[];
    onTileClick(at: GridPosition): void;
    onError(message: string): void;
};

export function Grid({ snapshot, play, sight, onTileClick, onError }: Props) {
    const host = useRef<HTMLDivElement>(null);
    const scene = useRef<Scene | null>(null);
    const latest = useRef({ snapshot, play, sight, onTileClick, onError });
    useEffect(() => {
        latest.current = { snapshot, play, sight, onTileClick, onError };
    });

    const { width, height } = snapshot;
    useEffect(() => {
        const element = host.current;
        if (element === null) {
            return;
        }
        let cancelled = false;
        let created: Scene | null = null;
        Scene.create(
            element,
            width,
            height,
            (at) => latest.current.onTileClick(at),
            (message) => latest.current.onError(message),
        ).then(
            (made) => {
                if (cancelled) {
                    made.destroy();
                    return;
                }
                created = made;
                scene.current = made;
                made.show(latest.current.snapshot, latest.current.play);
                made.showSight(latest.current.sight);
            },
            (error: unknown) => {
                if (!cancelled) {
                    latest.current.onError(`The grid could not be drawn: ${messageOf(error)}`);
                }
            },
        );
        return () => {
            cancelled = true;
            created?.destroy();
            scene.current = null;
        };
    }, [width, height]);

    useEffect(() => {
        scene.current?.show(snapshot, latest.current.play);
    }, [snapshot]);

    useEffect(() => {
        scene.current?.showSight(sight);
    }, [sight]);

    return <div ref={host} className="grid" />;
}
