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
import { marking, markedPalette, type Marking, type Marks } from "./markings";
import { frameAt, settledMs, transition, type Drawn, type Moment, type Transition } from "./motion";
import { rasterize, type Palette } from "./pixels";
import { isNewRun } from "./run";
import { meadow } from "./meadow";
import { ART_TILE, HEADROOM } from "./scale";
import sheet from "./sprites.json";
import { depth, drawn, placements, stacks, tuftOf } from "./tiles";
import type { EntityView, GeneRanges, GridPosition, PlayState, WorldSnapshot } from "./types";

type Kind = { palette: Palette; marks: Marks; frames: Partial<Record<string, string[]>> };

const KINDS: Partial<Record<string, Kind>> = { rabbit: sheet.rabbit, wolf: sheet.wolf };
const GROUND_COVER: Partial<Record<string, string[][]>> = { grass: sheet.grass.tufts };
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
    private entities = new Map<number, EntityView>();
    private genes: GeneRanges | null = null;
    private transition: Transition | null = null;
    private facing = new Map<number, boolean>();
    private started = 0;
    private previous: WorldSnapshot | null = null;
    private moment: Moment | null = null;
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
        const previous = this.previous;
        const sameRun = previous !== null && !isNewRun(previous, next);
        if (!sameRun) {
            this.markings.clear();
            this.facing = new Map();
        }
        const to: Moment = { day: next.day, placed: placements(next), drawn: drawn(next) };
        const from: Moment =
            sameRun && this.moment !== null
                ? this.moment
                : { day: next.day, placed: new Map(), drawn: new Set() };
        this.transition = transition(from, to, play, this.facing);
        this.facing = this.transition.facing;
        this.entities = new Map([...(sameRun ? this.entities : []), ...entitiesOf(next)]);
        for (const id of this.entities.keys()) {
            if (!from.placed.has(id) && !to.placed.has(id)) this.entities.delete(id);
        }
        this.started = performance.now();
        this.previous = next;
        this.moment = to;
        this.unsettled = next;
        if (!sameRun) this.settle();
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
        if (!placed.creature) {
            const tufts = GROUND_COVER[placed.spriteKey];
            if (tufts === undefined) return this.missing(placed.spriteKey);
            const tuft = tuftOf(placed.id) % tufts.length;
            return this.texture(`${placed.spriteKey}/${tuft}`, tufts[tuft], sheet.grass.palette);
        }
        const kind = KINDS[placed.spriteKey];
        if (kind === undefined) return this.missing(placed.spriteKey);
        const frame = kind.frames[placed.frame] !== undefined ? placed.frame : "stand";
        const grid = kind.frames[frame];
        if (grid === undefined) return this.missing(placed.spriteKey);
        const chosen = this.markingOf(placed.id);
        const key =
            chosen === null ? "plain" : `${chosen.colour}.${chosen.extent}.${chosen.accent}`;
        return this.texture(
            `${placed.spriteKey}/${frame}/${key}`,
            grid,
            chosen === null ? kind.palette : markedPalette(kind, chosen),
        );
    }

    /** Computed once per creature, since its genes never change; unmarked until the ranges arrive. */
    private markingOf(id: number): Marking | null {
        const cached = this.markings.get(id);
        if (cached !== undefined) return cached;
        const entity = this.entities.get(id);
        const ranges = entity === undefined ? undefined : this.genes?.[entity.species];
        if (entity === undefined || ranges === undefined) return null;
        const chosen = marking(entity.species, entity.genes, ranges);
        this.markings.set(id, chosen);
        return chosen;
    }

    private texture(key: string, grid: readonly string[], palette: Palette): Texture {
        const made = this.textures.get(key);
        if (made !== undefined) return made;
        const pixels = rasterize(grid, palette);
        const canvas = document.createElement("canvas");
        canvas.width = pixels.width;
        canvas.height = pixels.height;
        canvas
            .getContext("2d")
            ?.putImageData(new ImageData(pixels.data, pixels.width, pixels.height), 0, 0);
        const texture = new Texture({
            source: new CanvasSource({ resource: canvas, scaleMode: "nearest" }),
        });
        this.textures.set(key, texture);
        return texture;
    }

    /** The meadow is drawn once, with its headroom. */
    private groundTexture(): Texture {
        const pixels = meadow(this.width, this.height);
        const canvas = document.createElement("canvas");
        canvas.width = pixels.width;
        canvas.height = pixels.height;
        canvas
            .getContext("2d")
            ?.putImageData(new ImageData(pixels.data, pixels.width, pixels.height), 0, 0);
        const texture = new Texture({
            source: new CanvasSource({ resource: canvas, scaleMode: "nearest" }),
        });
        this.textures.set("ground", texture);
        return texture;
    }

    private missing(spriteKey: string): null {
        if (!this.reported.has(spriteKey)) {
            this.reported.add(spriteKey);
            this.onError(`There is no sprite for ${spriteKey}, so it is not drawn.`);
        }
        return null;
    }
}

function entitiesOf(snapshot: WorldSnapshot): [number, EntityView][] {
    return snapshot.tiles.flatMap((tile) =>
        tile.entities.map((entity): [number, EntityView] => [entity.id, entity]),
    );
}
