import { useEffect, useRef } from "react";
import { Scene } from "./scene";
import type { GridPosition, PlayState, WorldSnapshot } from "./types";

type Props = { snapshot: WorldSnapshot; play: PlayState; onTileClick(at: GridPosition): void };

export function Grid({ snapshot, play, onTileClick }: Props) {
    const host = useRef<HTMLDivElement>(null);
    const scene = useRef<Scene | null>(null);
    const latest = useRef({ snapshot, play, onTileClick });
    useEffect(() => {
        latest.current = { snapshot, play, onTileClick };
    });

    const { width, height } = snapshot;
    useEffect(() => {
        const element = host.current;
        if (element === null) {
            return;
        }
        let cancelled = false;
        let created: Scene | null = null;
        void Scene.create(element, width, height, (at) => latest.current.onTileClick(at)).then(
            (made) => {
                if (cancelled) {
                    made.destroy();
                    return;
                }
                created = made;
                scene.current = made;
                made.show(latest.current.snapshot, latest.current.play);
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

    return <div ref={host} className="grid" />;
}
