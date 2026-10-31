"""Pixel-art sources: each sprite is a 16 by 16 text grid, drawn at 2x on the page's 32-pixel tiles.

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

# 'g' fur, 'l' muzzle and chest.
WOLF_COLOURS = {"o": INK, "g": "#8e96a3", "l": "#e3e6ea"}

WOLF_IDLE = [
    "................",
    "................",
    ".....o.....o....",
    "....ogo...ogo...",
    "....oggoooggo...",
    "...ogggggggggo..",
    "...oggogggoggo..",
    "...oggllollggo..",
    "....oglllllgo...",
    "..oooooolllo....",
    ".oggggggglllo...",
    "oggggoggggllo...",
    "ogggogggggggo...",
    "ogo.ogggoggo....",
    "oo..oooooooo....",
    "................",
]


def render(rows, colours):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        if len(row) != 16:
            raise ValueError(f"row {y} is {len(row)} wide, not 16: {row}")
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
    path = out / "wolf" / "idle.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    render(WOLF_IDLE, WOLF_COLOURS).save(path)


if __name__ == "__main__":
    main()
