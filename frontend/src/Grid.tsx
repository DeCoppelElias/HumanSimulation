import { useEffect, useRef, useState } from "react";
import { messageOf } from "./messages";
import { wholeScale } from "./scale";
import { Scene } from "./scene";
import type { GeneRanges, GridPosition, PlayState, WorldSnapshot } from "./types";

type Props = {
    snapshot: WorldSnapshot;
    play: PlayState;
    sight: GridPosition[];
    genes: GeneRanges | null;
    onTileClick(at: GridPosition): void;
    onError(message: string): void;
};

export function Grid({ snapshot, play, sight, genes, onTileClick, onError }: Props) {
    const { width, height } = snapshot;
    const host = useRef<HTMLDivElement>(null);
    const scene = useRef<Scene | null>(null);
    const [scale, setScale] = useState(() =>
        wholeScale(width, height, window.innerWidth, window.innerHeight),
    );
    const latest = useRef({ snapshot, play, sight, genes, scale, onTileClick, onError });
    useEffect(() => {
        latest.current = { snapshot, play, sight, genes, scale, onTileClick, onError };
    });

    useEffect(() => {
        const resized = () =>
            setScale(wholeScale(width, height, window.innerWidth, window.innerHeight));
        resized();
        window.addEventListener("resize", resized);
        return () => window.removeEventListener("resize", resized);
    }, [width, height]);

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
            latest.current.scale,
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
                made.setScale(latest.current.scale);
                made.setGenes(latest.current.genes);
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

    useEffect(() => {
        scene.current?.setScale(scale);
    }, [scale]);

    useEffect(() => {
        scene.current?.setGenes(genes);
    }, [genes]);

    return <div ref={host} className="grid" />;
}
