from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
source = root / "artwork" / "park_animals_atlas.png"
output = root / "app" / "src" / "main" / "res" / "drawable-nodpi"
names = [
    "parque_caballo", "parque_conejo", "parque_elefante",
    "parque_pato", "parque_mono", "parque_leon",
]

atlas = Image.open(source).convert("RGBA")
cell_w, cell_h = atlas.width // 3, atlas.height // 2
for index, name in enumerate(names):
    column, row = index % 3, index // 3
    image = atlas.crop((
        column * cell_w,
        row * cell_h,
        (column + 1) * cell_w,
        (row + 1) * cell_h,
    ))
    pixels = image.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, _ = pixels[x, y]
            # Retira el fondo magenta y suaviza el halo del antialias.
            dominance = r + b - 2 * g
            if r > 190 and b > 170 and dominance > 170:
                alpha = max(0, min(255, 255 - (dominance - 170) * 3))
                pixels[x, y] = (r, g, b, alpha)
    bbox = image.getbbox()
    if bbox:
        image = image.crop(bbox)
    image.thumbnail((520, 520), Image.Resampling.LANCZOS)
    image.save(output / f"{name}.png", optimize=True)
    print(name, image.size)
