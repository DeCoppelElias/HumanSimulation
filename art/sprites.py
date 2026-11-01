"""Pixel-art sources: each sprite is a text grid with its palette and marking regions. Tiles are 16 by 16 art pixels.

Run `python art/sprites.py` to rewrite every PNG under art/png/ and frontend/src/sprites.json, which the page
draws from. Edit the grids, never the outputs.
Needs Pillow.
"""

import json
from pathlib import Path

from PIL import Image

INK = "#1a1f2b"
PINK = "#feacb6"

# 'o' ink outline, 'p' pink, 'w' white coat. The other letters are marking regions, white until a marking paints
# them: 't' tail, 'h' haunch, 'c' cap, 'e' ear edge, 'f' forehead, 'x' chest.
RABBIT_COLOURS = {"o": INK, "p": PINK, "w": "#ffffff", **{region: "#ffffff" for region in "thcefx"}}

# A marking's colour paints the regions its extent and accent name. 'base' recolours the whole coat, which the
# rabbit never does.
RABBIT_MARKS = {
    "colours": [
        {"base": {}, "marked": {region: colour for region in "thcefx"}}
        for colour in ["#9b6b4a", "#d9893b", "#7c8593", "#33363f", "#c9b27a", "#5f4436"]
    ],
    "extents": [[], ["t"], ["t", "h"], ["t", "h", "c", "e"]],
    "accents": [[], ["f"], ["x"]],
}

RABBIT_STAND = [
    "................",
    "......o...o.....",
    ".....oeo.oeo....",
    ".....opo.opo....",
    ".....opo.opo....",
    ".....oeo.oeo....",
    ".....oeoooeo....",
    "....occwffwwo...",
    "...occowwwowwo..",
    "..oocwwwowwwwo..",
    ".otohwwwwwwxwo..",
    "otthowwwwwwxo...",
    "ohhhwowwwwxxo...",
    ".ohhhwwowwowo...",
    "..oooooooooo....",
    "................",
]

RABBIT_BLINK = [
    "................",
    "......o...o.....",
    ".....oeo.oeo....",
    ".....opo.opo....",
    ".....opo.opo....",
    ".....oeo.oeo....",
    ".....oeoooeo....",
    "....occwffwwo...",
    "...occoowwoowo..",
    "..oocwwwowwwwo..",
    ".otohwwwwwwxwo..",
    "otthowwwwwwxo...",
    "ohhhwowwwwxxo...",
    ".ohhhwwowwowo...",
    "..oooooooooo....",
    "................",
]

RABBIT_CROUCH = [
    "................",
    "................",
    "......o...o.....",
    ".....oeo.oeo....",
    ".....opo.opo....",
    ".....oeo.oeo....",
    ".....oeoooeo....",
    "....occwffwwo...",
    "...occowwwowwo..",
    "..oocwwwowwwwo..",
    ".otohwwwwwwxwo..",
    "otthhowwwwwxwo..",
    ".ohhhwwowwoxxo..",
    "..ooooooooooo...",
    "................",
    "................",
]

RABBIT_AIR = [
    "......o...o.....",
    ".....oeo.oeo....",
    ".....opo.opo....",
    ".....opo.opo....",
    ".....oeo.oeo....",
    ".....oeoooeo....",
    "....occwffwwo...",
    "...occowwwowwo..",
    "..oocwwwowwwwo..",
    ".otohwwwwwwxwo..",
    "otthowwwwwwxwo..",
    "ohhhwowwwwxxo...",
    ".ohhhwwwwwwwo...",
    "..ohhwwwwwwo....",
    "...ooooooo......",
    "................",
]

RABBIT_AIR_LEGS = [
    "......o...o.....",
    ".....oeo.oeo....",
    ".....opo.opo....",
    ".....opo.opo....",
    ".....oeo.oeo....",
    ".....oeoooeo....",
    "....occwffwwo...",
    "...occowwwowwo..",
    "..oocwwwowwwwo..",
    ".otohwwwwwwxwo..",
    "otthowwwwwwxwo..",
    "ohhhwowwwwxxo...",
    "oohhhhwwwwwo....",
    "ohhoooooooo.....",
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
    ".....ocffwwo....",
    "....ocowwowo....",
    "...othwwwwxwo...",
    "...othhwwxwo....",
    "....ooooooo.....",
    "................",
    "................",
]

RABBIT_FRAMES = {
    "stand": RABBIT_STAND,
    "blink": RABBIT_BLINK,
    "crouch": RABBIT_CROUCH,
    "air": RABBIT_AIR,
    "air-legs": RABBIT_AIR_LEGS,
    "newborn": RABBIT_NEWBORN,
}

# The wolf is side-on and 24 by 18, so it overhangs its tile. 'g' slate fur, 'm' lighter back and thighs,
# 'l' muzzle, chest and near paws, 'd' and 'k' far legs and paws, 'p' inner ear. Marking regions: 'a' tail,
# 's' back, 'c' head top and ears, 'f' muzzle, 'x' near paws.
WOLF_COLOURS = {"o": INK, "g": "#56607a", "m": "#7e889e", "l": "#d6dae2", "d": "#3c4356", "k": "#9da4b3", "p": PINK,
                "a": "#56607a", "s": "#7e889e", "c": "#56607a", "f": "#56607a", "x": "#d6dae2"}


def mix(a, b, share):
    """The colour share of the way from a to b."""
    pa = [int(a[i:i + 2], 16) for i in (1, 3, 5)]
    pb = [int(b[i:i + 2], 16) for i in (1, 3, 5)]
    return "#" + "".join(f"{round(x + (y - x) * share):02x}" for x, y in zip(pa, pb))


CREAM = "#f2efe8"


def wolf_coat(coat):
    """A wolf marks in shades of its own coat: a darker saddle and paler muzzle or legs."""
    back = mix(coat, CREAM, 0.22)
    pale = mix(coat, CREAM, 0.68)
    dark = mix(coat, INK, 0.42)
    paler = mix(coat, CREAM, 0.8)
    return {
        "base": {"g": coat, "a": coat, "c": coat, "f": coat, "m": back, "s": back, "d": mix(coat, INK, 0.3),
                 "k": mix(coat, CREAM, 0.42), "l": pale, "x": pale},
        "marked": {"a": dark, "s": dark, "c": dark, "f": paler, "x": paler},
    }


# Light to dark to warm, so one step of drift lands on a coat near its parent's. A founder lands on the fourth, slate.
WOLF_MARKS = {
    "colours": [wolf_coat(coat) for coat in ["#858e9e", "#5f7186", "#6b6f7a", "#56607a", "#454a57", "#7a6d5c"]],
    "extents": [[], ["a"], ["a", "s"], ["a", "s", "c"]],
    "accents": [[], ["f"], ["x"]],
}

WOLF_STAND = [
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ocooco..",
    "................ocpocpo.",
    "...........ooooocccccco.",
    "......ooooosssssgggoffo.",
    "..oooogggggggggggggllllo",
    "ooaaggggggggggggglloooo.",
    "laaogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    ".....oggodo..oggodo.....",
    ".....oggodo..oggodo.....",
    ".....oxxoko..oxxoko.....",
    "......oooo....oooo......",
    "........................",
]

WOLF_BLINK = [
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ocooco..",
    "................ocpocpo.",
    "...........ooooocccccco.",
    "......ooooosssssggooffo.",
    "..oooogggggggggggggllllo",
    "ooaaggggggggggggglloooo.",
    "laaogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    ".....oggodo..oggodo.....",
    ".....oggodo..oggodo.....",
    ".....oxxoko..oxxoko.....",
    "......oooo....oooo......",
    "........................",
]

WOLF_STRIDE = [
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ocooco..",
    "................ocpocpo.",
    "...........ooooocccccco.",
    "......ooooosssssgggoffo.",
    "..oooogggggggggggggllllo",
    "ooaaggggggggggggglloooo.",
    "laaogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    ".....oggodo..odoggo.....",
    "....oggooddooddooggoo...",
    "...oxxo..okkokko.ooxxo..",
    "....oo....oo.oo....oo...",
    "........................",
]

WOLF_GATHER = [
    "........................",
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ocooco..",
    "................ocpocpo.",
    "...........ooooocccccco.",
    "......ooooosssssgggoffo.",
    "..oooogggggggggggggllllo",
    "ooaaggggggggggggglloooo.",
    "laaogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    ".....oggodo.odoggo......",
    "......oxxokokoxxo.......",
    ".......oooo.oooo........",
    "........................",
]

WOLF_TAKEOFF = [
    "........................",
    "........................",
    ".................o..oo..",
    "................ocoocpo.",
    "................ocpocgo.",
    "...............occcccfo.",
    "...........oooosgggofllo",
    "..........ossssgggglloo.",
    "......ooooggggggglloo...",
    "....oogggggggggggglo....",
    "..oogggggggggggggggo....",
    "ooaagggggggggggmmmo.....",
    "laaoogggggoooomdoggo....",
    "ooo..ommmmo...ookoxxo...",
    "....oogggoo.....oooo....",
    "...oggoddo..............",
    "..ooookko...............",
    ".oxxo.oo................",
]

WOLF_FLIGHT = [
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ocooco..",
    "................ocpocpo.",
    "...........ooooocccccco.",
    "......ooooosssssgggoffo.",
    "..oooogggggggggggggllllo",
    "ooaaggggggggggggglloooo.",
    "laaogggggggggggggglo....",
    "ooo.oggggggggggggggo....",
    ".....ommmmoooommmmo.....",
    "...oogggoo...ooogggoo...",
    ".ooggooddo..oddooooggoo.",
    "oxxoo.okko..okko...ooxxo",
    ".oo....oo....oo......oo.",
    "........................",
]

WOLF_LAND = [
    "........................",
    "........................",
    "........................",
    "........................",
    ".................o..o...",
    "................ocooco..",
    "..oo..oooo.oooo.ocpoco..",
    "ooaaooggggossssocccccpo.",
    "laaogggggggggggsgggoffo.",
    "oooogggggggggggggggllgo.",
    "....ogggggggggggglloollo",
    ".....ommmmgggggggglo.oo.",
    "....oggggooooomggggo....",
    "...oxxookko..odmmmo.....",
    "....oo..oo....oogggoo...",
    "..............oddooggo..",
    "..............okko.oxo..",
    "...............oo...oxo.",
]

WOLF_FRAMES = {
    "stand": WOLF_STAND,
    "blink": WOLF_BLINK,
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

# The meadow's shades for an early autumn field, from lush green through today's green to gold and straw.
# The second is GROUND_COLOURS itself.
GROUND_LUSH = "#2e7a3c"
GROUND_GOLD = "#d2b03c"
GROUND_STRAW = "#c4a95a"
GROUND_SHADES = [
    {letter: mix(colour, GROUND_LUSH, 0.18) for letter, colour in GROUND_COLOURS.items()},
    GROUND_COLOURS,
    {letter: mix(colour, GROUND_GOLD, 0.26) for letter, colour in GROUND_COLOURS.items()},
    {letter: mix(colour, GROUND_STRAW, 0.4) for letter, colour in GROUND_COLOURS.items()},
]

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


def marked(palette, marks, colour, extent, accent):
    """The palette with one marking painted on, the way the page paints it."""
    shade = marks["colours"][colour]
    painted = {**palette, **shade["base"]}
    for region in marks["extents"][extent] + marks["accents"][accent]:
        painted[region] = shade["marked"][region]
    return painted


# Where a genome with no drift from its species' baseline lands.
FOUNDER = (3, 2, 1)


def main():
    out = Path(__file__).parent / "png"
    for name, frames, palette, marks in (
        ("rabbit", RABBIT_FRAMES, RABBIT_COLOURS, RABBIT_MARKS),
        ("wolf", WOLF_FRAMES, WOLF_COLOURS, WOLF_MARKS),
    ):
        (out / name).mkdir(parents=True, exist_ok=True)
        for frame, rows in frames.items():
            render(rows, palette).save(out / name / f"{frame}.png")
            render(rows, marked(palette, marks, *FOUNDER)).save(out / name / f"{frame}-founder.png")
    (out / "grass").mkdir(parents=True, exist_ok=True)
    for i, rows in enumerate(GRASS_TUFTS):
        render(rows, GRASS_COLOURS).save(out / "grass" / f"tuft-{i}.png")
    for i, rows in enumerate(GROUND_TILES):
        render(rows, GROUND_COLOURS).save(out / "grass" / f"ground-{i}.png")
    sheet = {
        "rabbit": {"palette": RABBIT_COLOURS, "marks": RABBIT_MARKS, "frames": RABBIT_FRAMES},
        "wolf": {"palette": WOLF_COLOURS, "marks": WOLF_MARKS, "frames": WOLF_FRAMES},
        "grass": {"palette": GRASS_COLOURS, "tufts": GRASS_TUFTS},
        "ground": {"palette": GROUND_COLOURS, "shades": GROUND_SHADES, "tiles": GROUND_TILES},
    }
    page = Path(__file__).parent.parent / "frontend" / "src" / "sprites.json"
    page.write_text(json.dumps(sheet, indent=1) + "\n", encoding="utf-8", newline="\n")


if __name__ == "__main__":
    main()
