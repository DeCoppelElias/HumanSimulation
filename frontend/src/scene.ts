import {
    Application,
    Assets,
    Container,
    Graphics,
    Rectangle,
    Sprite,
    Text,
    Texture,
} from "pixi.js";
import { finished, frameAt, timing, type Transition } from "./motion";
import { isNewRun } from "./run";
import { drawn, placements, stacks } from "./tiles";
import type { GridPosition, PlayState, WorldSnapshot } from "./types";

export const TILE = 32;
const SPRITE_SIZE = TILE * 0.9;

/** The PixiJS side of the grid, drawn imperatively; React only hands it snapshots. */
export class Scene {
    private readonly sprites = new Container();
    private readonly labels = new Container();
    private readonly pool = new Map<number, Sprite>();
    private readonly textures = new Map<string, Texture>();
    private readonly loading = new Set<string>();
    private readonly failed = new Set<string>();
    private transition: Transition | null = null;
    private started = 0;
    private previous: WorldSnapshot | null = null;

    private constructor(
        private readonly app: Application,
        private readonly onError: (message: string) => void,
    ) {}

    static async create(
        host: HTMLElement,
        width: number,
        height: number,
        onTileClick: (at: GridPosition) => void,
        onError: (message: string) => void,
    ): Promise<Scene> {
        const app = new Application();
        await app.init({ width: width * TILE, height: height * TILE, background: "#84c669" });
        const scene = new Scene(app, onError);
        try {
            scene.textures.set("rabbit", await Assets.load<Texture>("/sprites/rabbit.png"));
        } catch (error) {
            scene.destroy();
            throw error;
        }
        app.stage.addChild(tileLines(width, height), scene.sprites, scene.labels);
        app.stage.eventMode = "static";
        app.stage.hitArea = new Rectangle(0, 0, width * TILE, height * TILE);
        app.stage.on("pointertap", (event) => {
            onTileClick({
                x: Math.floor(event.global.x / TILE),
                y: Math.floor(event.global.y / TILE),
            });
        });
        app.ticker.add(() => scene.draw());
        host.appendChild(app.canvas);
        return scene;
    }

    show(next: WorldSnapshot, play: PlayState): void {
        const previous = this.previous;
        const sameRun = previous !== null && !isNewRun(previous, next);
        this.transition = {
            from: sameRun ? placements(previous) : new Map(),
            to: placements(next),
            fromDrawn: sameRun ? drawn(previous) : new Set(),
            toDrawn: drawn(next),
            timing: timing(sameRun ? next.day - previous.day : 0, play),
        };
        this.started = performance.now();
        this.previous = next;
        this.drawCounts(next);
    }

    destroy(): void {
        this.app.destroy(true, { children: true });
    }

    private draw(): void {
        const transition = this.transition;
        if (transition === null) {
            return;
        }
        const elapsed = performance.now() - this.started;
        const frame = frameAt(transition, elapsed);
        const seen = new Set<number>();
        let waiting = false;
        for (const placed of frame) {
            const texture = this.texture(placed.spriteKey);
            if (texture === null) {
                waiting ||= !this.failed.has(placed.spriteKey);
                continue;
            }
            let sprite = this.pool.get(placed.id);
            if (sprite === undefined) {
                sprite = new Sprite(texture);
                sprite.anchor.set(0.5);
                sprite.scale.set(SPRITE_SIZE / Math.max(texture.width, texture.height));
                this.pool.set(placed.id, sprite);
                this.sprites.addChild(sprite);
            }
            sprite.position.set((placed.x + 0.5) * TILE, (placed.y + 0.5) * TILE);
            sprite.alpha = placed.alpha;
            seen.add(placed.id);
        }
        for (const [id, sprite] of this.pool) {
            if (!seen.has(id)) {
                sprite.destroy();
                this.pool.delete(id);
            }
        }
        // A sprite whose texture is still loading keeps the frame alive until it can be drawn.
        if (finished(transition, elapsed) && !waiting) {
            this.transition = null;
        }
    }

    private drawCounts(snapshot: WorldSnapshot): void {
        this.labels.removeChildren().forEach((child) => child.destroy());
        for (const stack of stacks(snapshot)) {
            if (stack.count > 1) {
                const label = new Text({
                    text: String(stack.count),
                    style: {
                        fontSize: 12,
                        fontWeight: "bold",
                        fill: "#ffffff",
                        stroke: { color: "#000000", width: 3 },
                    },
                });
                label.anchor.set(1, 0);
                label.position.set((stack.x + 1) * TILE - 2, stack.y * TILE + 1);
                this.labels.addChild(label);
            }
        }
    }

    /** A texture loads once, on first sight of its key; until then, or if it fails, that species is not drawn. */
    private texture(key: string): Texture | null {
        const loaded = this.textures.get(key);
        if (loaded !== undefined) {
            return loaded;
        }
        if (!this.loading.has(key)) {
            this.loading.add(key);
            Assets.load<Texture>(`/sprites/${key}.png`).then(
                (texture) => this.textures.set(key, texture),
                () => {
                    this.failed.add(key);
                    this.onError(`There is no sprite for ${key}, so it is not drawn.`);
                },
            );
        }
        return null;
    }
}

function tileLines(width: number, height: number): Graphics {
    const lines = new Graphics();
    for (let x = 1; x < width; x++) {
        lines.moveTo(x * TILE, 0).lineTo(x * TILE, height * TILE);
    }
    for (let y = 1; y < height; y++) {
        lines.moveTo(0, y * TILE).lineTo(width * TILE, y * TILE);
    }
    return lines.stroke({ color: "#000000", width: 1, alpha: 0.08 });
}
