import {
    Application,
    CanvasSource,
    Container,
    Graphics,
    Rectangle,
    Sprite,
    Texture,
} from "pixi.js";
import { chip } from "./chip";
import { lookOf } from "./looks";
import { marking, type Marking } from "./markings";
import { meadow } from "./meadow";
import { frameAt, settledMs, type Drawn, type Transition } from "./motion";
import { advance, START, type Playback } from "./playback";
import { rasterize, type Palette, type Pixels } from "./pixels";
import { ART_TILE, HEADROOM } from "./scale";
import sheet from "./sprites.json";
import { depth, stacks } from "./tiles";
import type { GeneRanges, GridPosition, PlayState, WorldSnapshot } from "./types";

const CHIP_COLOURS = { o: "#1a1f2b", w: "#ffffff" };

/** The PixiJS side of the grid, drawn imperatively in art pixels on a stage scaled by a whole number. */
export class Scene {
    private readonly cover = new Container();
    private readonly sightLayer = new Graphics();
    private readonly creatures = new Container();
    private readonly chips = new Container();
    private readonly pool = new Map<number, Sprite>();
    private readonly textures = new Map<string, Texture>();
    private readonly markings = new Map<number, Marking | null>();
    private readonly reported = new Set<string>();
    private playback: Playback = START;
    private genes: GeneRanges | null = null;
    private transition: Transition | null = null;
    private started = 0;
    /** The day whose chips wait for its moves to land. */
    private unsettled: WorldSnapshot | null = null;

    private constructor(
        private readonly app: Application,
        private readonly width: number,
        private readonly height: number,
        private readonly onError: (message: string) => void,
    ) {}

    static async create(
        host: HTMLElement,
        width: number,
        height: number,
        scale: number,
        onTileClick: (at: GridPosition) => void,
        onError: (message: string) => void,
    ): Promise<Scene> {
        const app = new Application();
        await app.init({
            width: width * ART_TILE * scale,
            height: (height * ART_TILE + HEADROOM) * scale,
            background: sheet.ground.palette.G,
            antialias: false,
            roundPixels: true,
        });
        const scene = new Scene(app, width, height, onError);
        scene.creatures.sortableChildren = true;
        app.stage.scale.set(scale);
        const ground = new Sprite(scene.groundTexture());
        ground.y = -HEADROOM;
        const world = new Container();
        world.y = HEADROOM;
        world.addChild(ground, scene.cover, scene.sightLayer, scene.creatures, scene.chips);
        app.stage.addChild(world);
        world.eventMode = "static";
        world.hitArea = new Rectangle(0, 0, width * ART_TILE, height * ART_TILE);
        world.on("pointertap", (event) => {
            const at = event.getLocalPosition(world);
            onTileClick({ x: Math.floor(at.x / ART_TILE), y: Math.floor(at.y / ART_TILE) });
        });
        app.ticker.add(() => scene.draw());
        host.appendChild(app.canvas);
        return scene;
    }

    setScale(scale: number): void {
        this.app.renderer.resize(
            this.width * ART_TILE * scale,
            (this.height * ART_TILE + HEADROOM) * scale,
        );
        this.app.stage.scale.set(scale);
    }

    setGenes(genes: GeneRanges | null): void {
        this.genes = genes;
        this.markings.clear();
    }

    show(next: WorldSnapshot, play: PlayState): void {
        const step = advance(this.playback, next, play);
        if (step.newRun) {
            for (const sprite of this.pool.values()) sprite.destroy();
            this.pool.clear();
            this.markings.clear();
        }
        for (const id of step.gone) this.markings.delete(id);
        this.playback = step.state;
        this.transition = step.transition;
        this.started = performance.now();
        this.unsettled = next;
        if (step.newRun) this.settle();
    }

    showSight(tiles: GridPosition[]): void {
        this.sightLayer.clear();
        for (const at of tiles) {
            this.sightLayer.rect(at.x * ART_TILE, at.y * ART_TILE, ART_TILE, ART_TILE);
        }
        if (tiles.length > 0) {
            this.sightLayer.fill({ color: "#fff7a8", alpha: 0.45 });
        }
    }

    destroy(): void {
        this.app.destroy(true, { children: true });
        for (const texture of this.textures.values()) texture.destroy(true);
    }

    private draw(): void {
        const transition = this.transition;
        if (transition === null) {
            return;
        }
        const now = performance.now();
        if (this.unsettled !== null && now - this.started >= settledMs(transition)) {
            this.settle();
        }
        const seen = new Set<number>();
        for (const placed of frameAt(transition, now - this.started, now)) {
            const texture = this.textureOf(placed);
            if (texture === null) {
                continue;
            }
            let sprite = this.pool.get(placed.id);
            if (sprite === undefined) {
                sprite = new Sprite(texture);
                sprite.anchor.set(0.5, 1);
                this.pool.set(placed.id, sprite);
                (placed.creature ? this.creatures : this.cover).addChild(sprite);
            }
            sprite.texture = texture;
            sprite.position.set(
                Math.round(placed.x * ART_TILE + ART_TILE / 2 + placed.ox),
                Math.round(placed.y * ART_TILE + ART_TILE + placed.oy - placed.lift),
            );
            sprite.scale.x = placed.flipped ? -1 : 1;
            sprite.zIndex = depth(placed);
            sprite.alpha = placed.alpha;
            seen.add(placed.id);
        }
        for (const [id, sprite] of this.pool) {
            if (!seen.has(id)) {
                sprite.destroy();
                this.pool.delete(id);
            }
        }
    }

    private settle(): void {
        if (this.unsettled !== null) this.drawChips(this.unsettled);
        this.unsettled = null;
    }

    private drawChips(snapshot: WorldSnapshot): void {
        this.chips.removeChildren().forEach((child) => child.destroy());
        for (const stack of stacks(snapshot)) {
            const grid = chip(stack.creatures);
            if (grid === null) continue;
            const sprite = new Sprite(this.texture(`chip/${stack.creatures}`, grid, CHIP_COLOURS));
            sprite.position.set((stack.x + 1) * ART_TILE - grid[0].length, stack.y * ART_TILE);
            this.chips.addChild(sprite);
        }
    }

    private textureOf(placed: Drawn): Texture | null {
        const look = lookOf(placed, placed.creature ? this.markingOf(placed.id) : null);
        if (look === null) {
            if (!this.reported.has(placed.spriteKey)) {
                this.reported.add(placed.spriteKey);
                this.onError(`There is no sprite for ${placed.spriteKey}, so it is not drawn.`);
            }
            return null;
        }
        return this.texture(look.key, look.grid, look.palette);
    }

    /** Computed once per creature, since its genes never change; unmarked until the ranges arrive. */
    private markingOf(id: number): Marking | null {
        const cached = this.markings.get(id);
        if (cached !== undefined) return cached;
        const entity = this.playback.entities.get(id);
        const ranges = entity === undefined ? undefined : this.genes?.[entity.species];
        if (entity === undefined || ranges === undefined) return null;
        const chosen = marking(entity.species, entity.genes, ranges);
        this.markings.set(id, chosen);
        return chosen;
    }

    private texture(key: string, grid: readonly string[], palette: Palette): Texture {
        const made = this.textures.get(key);
        if (made !== undefined) return made;
        const texture = textureFrom(rasterize(grid, palette));
        this.textures.set(key, texture);
        return texture;
    }

    /** The meadow is drawn once, with its headroom. */
    private groundTexture(): Texture {
        const texture = textureFrom(meadow(this.width, this.height));
        this.textures.set("ground", texture);
        return texture;
    }
}

function textureFrom(pixels: Pixels): Texture {
    const canvas = document.createElement("canvas");
    canvas.width = pixels.width;
    canvas.height = pixels.height;
    canvas
        .getContext("2d")
        ?.putImageData(new ImageData(pixels.data, pixels.width, pixels.height), 0, 0);
    return new Texture({ source: new CanvasSource({ resource: canvas, scaleMode: "nearest" }) });
}
