from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
output = root / "app" / "src" / "main" / "res" / "drawable-nodpi"
names = ["tren_rosa", "tren_violeta", "tren_contraste"]

for name in names:
    path = output / f"{name}.png"
    image = Image.open(path).convert("RGBA")
    pixels = image.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            # Retira el fondo verde (chroma key) y suaviza el halo del antialias.
            dominance = g - max(r, b)
            if g > 150 and dominance > 40:
                alpha = max(0, min(255, 255 - (dominance - 40) * 4))
                pixels[x, y] = (r, g, b, alpha)
    bbox = image.getbbox()
    if bbox:
        image = image.crop(bbox)
    image.thumbnail((720, 720), Image.Resampling.LANCZOS)
    image.save(path, optimize=True)
    print(name, image.size)
