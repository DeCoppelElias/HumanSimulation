"""Pixel-art sources: each sprite is a text grid, drawn at 2x on the page's 32-pixel tiles. Rabbits are 16 by 16.

Run `python art/sprites.py` to rewrite every PNG under art/png/. Edit the grids, never the PNGs.
Needs Pillow.
"""

from pathlib import Path

from PIL import Image

INK = "#1a1f2b"
PINK = "#feacb6"

# 'o' ink outline, 'p' pink, 'w' coat, 'd' second coat colour, which equals 'w' on one-tone coats.
RABBIT_COATS = {
    "white": {"w": "#ffffff", "d": "#ffffff"},
    "brown": {"w": "#c9a27c", "d": "#c9a27c"},
    "grey": {"w": "#aab1bb", "d": "#aab1bb"},
    "dutch": {"w": "#ffffff", "d": "#6b5a52"},
}

RABBIT_IDLE = [
    "................",
    "......o...o.....",
    ".....odo.odo....",
    ".....opo.opo....",
    ".....opo.opo....",
    ".....odo.odo....",
    ".....odooodo....",
    "....oddwwwwwo...",
    "...oddowwwowwo..",
    "..oodwwwowwwwo..",
    ".owodwwwwwwwwo..",
    "owddowwwwwwwo...",
    "odddwowwwwwwo...",
    ".odddwwowwowo...",
    "..oooooooooo....",
    "................",
]

RABBIT_BLINK = RABBIT_IDLE[:8] + ["...oddoowwoowo.."] + RABBIT_IDLE[9:]

RABBIT_CROUCH = [
    "................",
    "................",
    "......o...o.....",
    ".....odo.odo....",
    ".....opo.opo....",
    ".....odo.odo....",
    ".....odooodo....",
    "....oddwwwwwo...",
    "...oddowwwowwo..",
    "..oodwwwowwwwo..",
    ".owodwwwwwwwwo..",
    "owdddowwwwwwwo..",
    ".odddwwowwowwo..",
    "..ooooooooooo...",
    "................",
    "................",
]

RABBIT_AIR = [
    "......o...o.....",
    ".....odo.odo....",
    ".....opo.opo....",
    ".....opo.opo....",
    ".....odo.odo....",
    ".....odooodo....",
    "....oddwwwwwo...",
    "...oddowwwowwo..",
    "..oodwwwowwwwo..",
    ".owodwwwwwwwwo..",
    "owddowwwwwwwwo..",
    "odddwowwwwwwo...",
    ".odddwwwwwwwo...",
    "..oddwwwwwwo....",
    "...ooooooo......",
    "................",
]

RABBIT_AIR_LEGS = RABBIT_AIR[:11] + [
    "odddwowwwwwwo...",
    "ooddddwwwwwo....",
    "oddoooooooo.....",
    "ooo.............",
    "................",
]

RABBIT_NEWBORN = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    ".......o.o......",
    "......opopo.....",
    ".....odwwwwo....",
    "....odowwowo....",
    "...oddwwwwwwo...",
    "...odddwwwwo....",
    "....ooooooo.....",
    "................",
    "................",
]

RABBIT_FRAMES = {
    "idle": RABBIT_IDLE,
    "blink": RABBIT_BLINK,
    "crouch": RABBIT_CROUCH,
    "air": RABBIT_AIR,
    "air-legs": RABBIT_AIR_LEGS,
    "newborn": RABBIT_NEWBORN,
}

# The wolf is side-on and 24 by 18, so it overhangs its tile. 'g' slate fur, 'm' lighter back and thighs,
# 'l' muzzle, chest and near paws, 'd' and 'k' far legs and paws, 'p' inner ear.
WOLF_COLOURS = {"o": INK, "g": "#56607a", "m": "#7e889e", "l": "#d6dae2", "d": "#3c4356", "k": "#9da4b3", "p": PINK}

WOLF_STAND = [
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ogoogo..",
    "................ogpogpo.",
    "...........oooooggggggo.",
    "......ooooommmmmgggoggo.",
    "..oooogggggggggggggllllo",
    "ooggggggggggggggglloooo.",
    "lggogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    ".....oggodo..oggodo.....",
    ".....oggodo..oggodo.....",
    ".....olloko..olloko.....",
    "......oooo....oooo......",
    "........................",
]

WOLF_STRIDE = [
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ogoogo..",
    "................ogpogpo.",
    "...........oooooggggggo.",
    "......ooooommmmmgggoggo.",
    "..oooogggggggggggggllllo",
    "ooggggggggggggggglloooo.",
    "lggogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    ".....oggodo..odoggo.....",
    "....oggooddooddooggoo...",
    "...ollo..okkokko.oollo..",
    "....oo....oo.oo....oo...",
    "........................",
]

WOLF_GATHER = [
    "........................",
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ogoogo..",
    "................ogpogpo.",
    "...........oooooggggggo.",
    "......ooooommmmmgggoggo.",
    "..oooogggggggggggggllllo",
    "ooggggggggggggggglloooo.",
    "lggogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    ".....oggodo.odoggo......",
    "......ollokokollo.......",
    ".......oooo.oooo........",
    "........................",
]

WOLF_TAKEOFF = [
    "........................",
    "........................",
    ".................o..oo..",
    "................ogoogpo.",
    "................ogpoggo.",
    "...............oggggggo.",
    "...........oooomgggogllo",
    "..........ommmmgggglloo.",
    "......ooooggggggglloo...",
    "....oogggggggggggglo....",
    "..oogggggggggggggggo....",
    "oogggggggggggggmmmo.....",
    "lggoogggggoooomdoggo....",
    "ooo..ommmmo...ookollo...",
    "....oogggoo.....oooo....",
    "...oggoddo..............",
    "..ooookko...............",
    ".ollo.oo................",
]

WOLF_FLIGHT = [
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ogoogo..",
    "................ogpogpo.",
    "...........oooooggggggo.",
    "......ooooommmmmgggoggo.",
    "..oooogggggggggggggllllo",
    "ooggggggggggggggglloooo.",
    "lggogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    "...oogggoo...ooogggoo...",
    ".ooggooddo..oddooooggoo.",
    "olloo.okko..okko...oollo",
    ".oo....oo....oo......oo.",
    "........................",
]

WOLF_LAND = [
    "........................",
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ogoogo..",
    "..oo..oooo.oooo.ogpogo..",
    "ooggooggggommmmogggggpo.",
    "lggogggggggggggmgggoggo.",
    "oooogggggggggggggggllgo.",
    "....ogggggggggggglloollo",
    ".....ommmmgggggggglo.oo.",
    "....oggggooooomggggo....",
    "...ollookko..odmmmo.....",
    "....oo..oo....oogggoo...",
    "..............oddooggo..",
    "..............okko.olo..",
    "...............oo...olo.",
]

WOLF_FRAMES = {
    "stand": WOLF_STAND,
    "stride": WOLF_STRIDE,
    "gather": WOLF_GATHER,
    "takeoff": WOLF_TAKEOFF,
    "flight": WOLF_FLIGHT,
    "land": WOLF_LAND,
}


# Three tufts, picked per tile. 'o' outline, 'd' back blades, 'f' short shaded front blades, 'm' middle blades,
# 'l' front blades, 'h' tips. 'S' and 'T' shade the ground under the tuft, so it has no outline along the bottom.
GRASS_COLOURS = {"o": "#1f5a37", "d": "#20813d", "f": "#1a6e36", "m": "#3fae36", "l": "#68d736", "h": "#a6ec5c", "S": "#46a832", "T": "#3d9a2d"}

GRASS_TUFTS = [
    [
        "................",
        "........o.......",
        ".....o.odo......",
        "....odoodooo....",
        "....odooddldo...",
        "...oldddddmdo...",
        ".ooomodddmmddoo.",
        "olmommddhmddomlo",
        ".ommohddlmddmmo.",
        "..ommlldlmdhmo..",
        "..ohffldlmllmo..",
        "...olfldlfmmlho.",
        "...olfldlflmlo..",
        "...SdTdTdTTddS..",
        ".....STTTTTS....",
        "................",
    ],
    [
        "................",
        ".......o........",
        "......odo.o.....",
        "....ooodoodo....",
        "...odlddoodo....",
        "...odmdddddlo...",
        ".ooddmmdddomooo.",
        "olmoddmhddmmomlo",
        ".ommddmlddhommo.",
        "..omhdmldllmmo..",
        "..omllmldlffho..",
        ".ohlmmfldlflo...",
        "..olmlfldlflo...",
        "..SddTTdTdTdS...",
        "....STTTTTS.....",
        "................",
    ],
    [
        "................",
        "................",
        "................",
        "................",
        "................",
        "......o.........",
        ".....odoo.o.....",
        ".....odolodo....",
        ".....oddmddo....",
        "....ooodhddoo...",
        "...olhldlddmlo..",
        "....omldmdlho...",
        ".....oldfdlo....",
        ".....SdTTTdS....",
        ".......STS......",
        "................",
    ],
]

# Four ground tiles, picked per tile so the marks don't repeat. 'G' ground, 't' blade marks, 's' light flecks.
GROUND_COLOURS = {"G": "#58c135", "t": "#47aa30", "s": "#6fd046"}

GROUND_TILES = [
    [
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGs",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGsGGGGGGGGG",
        "GGGGGGGGtGtstsGG",
        "GGGGGGGGtGGtGGGG",
        "GGGGGGGGGGGGGGGG",
    ],
    [
        "GGGGGGGGGGGGGGsG",
        "GGGGGGGGGGGGGGGG",
        "GtGGsGGGGGGGGGGG",
        "GGtGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGsGGG",
        "GGGGGGtGGsGGGGGG",
        "GGGGGGGtGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
    ],
    [
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGtGtGGGGGGGG",
        "GGGGGGtGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGsG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGtGssGG",
        "GGGGGGGGGGGtGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
    ],
    [
        "sGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGtGtGGGGG",
        "GGGGGGGstGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGtGtGGGGGG",
        "GGGGGGGtGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGGG",
    ],
]

def render(rows, colours):
    width = len(rows[0])
    image = Image.new("RGBA", (width, len(rows)), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        if len(row) != width:
            raise ValueError(f"row {y} is {len(row)} wide, not {width}: {row}")
        for x, cell in enumerate(row):
            if cell in colours:
                hex_ = colours[cell]
                image.putpixel((x, y), (int(hex_[1:3], 16), int(hex_[3:5], 16), int(hex_[5:7], 16), 255))
    return image


def main():
    out = Path(__file__).parent / "png"
    for frame, rows in RABBIT_FRAMES.items():
        for coat, colours in RABBIT_COATS.items():
            path = out / "rabbit" / f"{frame}-{coat}.png"
            path.parent.mkdir(parents=True, exist_ok=True)
            render(rows, {"o": INK, "p": PINK, **colours}).save(path)
    for frame, rows in WOLF_FRAMES.items():
        path = out / "wolf" / f"{frame}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        render(rows, WOLF_COLOURS).save(path)
    (out / "grass").mkdir(parents=True, exist_ok=True)
    for i, rows in enumerate(GRASS_TUFTS):
        render(rows, GRASS_COLOURS).save(out / "grass" / f"tuft-{i}.png")
    for i, rows in enumerate(GROUND_TILES):
        render(rows, GROUND_COLOURS).save(out / "grass" / f"ground-{i}.png")


if __name__ == "__main__":
    main()
