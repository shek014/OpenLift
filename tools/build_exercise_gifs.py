#!/usr/bin/env python3
"""Builds the exercise GIFs in the app's assets from the Everkinetic drawings.

Each drawing comes as two separately cropped images, one for each end of the movement. This lines
them up on the parts that don't move, such as the bench or the feet, so that only the moving parts
appear to move in the GIF.

Usage: tools/build_exercise_gifs.py <download folder>
Needs ImageMagick. The drawings are CC BY-SA 4.0, and so are the GIFs made from them.
"""

import subprocess
import sys
import urllib.request
from pathlib import Path

# The Everkinetic commit to download from, so that rebuilding always gives the same GIFs.
COMMIT = "446bb9a3d0c3beb6b84f7c9d77dfc8af707a2ab6"
SOURCE_URL = f"https://raw.githubusercontent.com/everkinetic/data/{COMMIT}/"

ROOT = Path(__file__).resolve().parent.parent
MAPPING = ROOT / "tools" / "everkinetic_gifs.tsv"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "exercise_media"

GIF_WIDTH = 480
FRAME_DELAY = 90  # In hundredths of a second.
ALIGN_WIDTH = 240  # The size to compare the images at. Higher is slower and no more useful.


def download(path: str, folder: Path) -> Path:
    target = folder / path.replace("/", "_")
    if not target.exists():
        urllib.request.urlretrieve(SOURCE_URL + path, target)
    return target


def size(image: Path) -> tuple[int, int]:
    result = subprocess.run(
        ["magick", "identify", "-format", "%w %h", str(image)],
        check=True, capture_output=True, text=True,
    )
    width, height = result.stdout.split()
    return int(width), int(height)


def dark_pixel_rows(image: Path, scale: float) -> list[int]:
    """Returns the image as one integer per row, where each set bit is a dark pixel."""
    width, height = size(image)
    small = f"{max(1, round(width * scale))}x{max(1, round(height * scale))}!"
    result = subprocess.run(
        ["magick", str(image), "-background", "white", "-flatten", "-colorspace", "Gray",
         "-resize", small, "-depth", "8", "pgm:-"],
        check=True, capture_output=True,
    )
    # A binary PGM file has a text header of "P5", the width, the height and the maximum value.
    fields, position = [], 0
    while len(fields) < 4:
        end = position
        while not result.stdout[end:end + 1].isspace():
            end += 1
        fields.append(result.stdout[position:end])
        position = end + 1
    columns, lines = int(fields[1]), int(fields[2])
    pixels = result.stdout[position:]
    rows = []
    for line in range(lines):
        row = pixels[line * columns:(line + 1) * columns]
        rows.append(sum(1 << column for column, value in enumerate(row) if value < 128))
    return rows


def best_offset(first: Path, second: Path) -> tuple[int, int]:
    """Finds how far to move the second image so that its lines overlap the first's the most."""
    scale = ALIGN_WIDTH / max(size(first)[0], size(second)[0])
    rows_a, rows_b = dark_pixel_rows(first, scale), dark_pixel_rows(second, scale)
    reach_x, reach_y = ALIGN_WIDTH // 2, max(len(rows_a), len(rows_b)) // 2
    best_score, best = -1, (0, 0)
    for dy in range(-reach_y, reach_y + 1):
        for dx in range(-reach_x, reach_x + 1):
            score = 0
            for line, row in enumerate(rows_b):
                other = line + dy
                if 0 <= other < len(rows_a):
                    moved = row << dx if dx >= 0 else row >> -dx
                    score += (rows_a[other] & moved).bit_count()
            if score > best_score:
                best_score, best = score, (dx, dy)
    return round(best[0] / scale), round(best[1] / scale)


def build_gif(exercise_id: str, first: Path, second: Path) -> None:
    dx, dy = best_offset(first, second)
    (width_a, height_a), (width_b, height_b) = size(first), size(second)
    left, top = min(0, dx), min(0, dy)
    right, bottom = max(width_a, width_b + dx), max(height_a, height_b + dy)
    padding = 20
    canvas = f"{right - left + 2 * padding}x{bottom - top + 2 * padding}"

    def frame(image: Path, x: int, y: int) -> list[str]:
        return ["(", "-size", canvas, "xc:white", str(image), "-geometry",
                f"+{x - left + padding}+{y - top + padding}", "-composite", ")"]

    subprocess.run(
        ["magick", "-quiet", "-delay", str(FRAME_DELAY), "-loop", "0",
         *frame(first, 0, 0), *frame(second, dx, dy),
         "-colorspace", "Gray", "-resize", f"{GIF_WIDTH}x",
         # Fixed gray levels, so that the paper comes out pure white like the panel it's shown on.
         "+dither", "-posterize", "8", "-layers", "optimize",
         str(OUTPUT / f"{exercise_id}.gif")],
        check=True,
    )


def main() -> None:
    folder = Path(sys.argv[1])
    folder.mkdir(parents=True, exist_ok=True)
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for line in MAPPING.read_text().splitlines():
        if line.startswith("#") or not line.strip():
            continue
        exercise_id, first, second = line.split("\t")
        # Aligning is slow, so only build what's missing. Delete a GIF to rebuild it.
        if not (OUTPUT / f"{exercise_id}.gif").exists():
            build_gif(exercise_id, download(first, folder), download(second, folder))
            print(exercise_id)


if __name__ == "__main__":
    main()
