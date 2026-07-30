"""Separa el atlas 4x3 y elimina el verde sólido para crear iconos transparentes."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "artwork" / "color_mascots_atlas.png"
OUTPUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
NAMES = [
    "color_rojo", "color_azul", "color_amarillo", "color_verde",
    "color_rosa", "color_naranja", "color_morado", "color_turquesa",
    "color_cafe", "color_negro", "color_blanco", "color_gris",
]

atlas = Image.open(SOURCE).convert("RGBA")
w, h = atlas.size
for i, name in enumerate(NAMES):
    row, col = divmod(i, 4)
    # Quita el canal blanco que separa las celdas.
    inset_x, inset_y = int(w / 4 * .018), int(h / 3 * .018)
    box = (
        round(col*w/4)+inset_x, round(row*h/3)+inset_y,
        round((col+1)*w/4)-inset_x, round((row+1)*h/3)-inset_y,
    )
    icon = atlas.crop(box)
    pixels = icon.load()
    for y in range(icon.height):
        for x in range(icon.width):
            r, g, b, a = pixels[x, y]
            # Chroma suave: elimina verde y reduce el halo de los bordes.
            dominance = g - max(r, b)
            if g > 145 and dominance > 55:
                alpha = max(0, min(255, int(255 * (1 - (dominance-55)/115))))
                pixels[x, y] = (r, min(g, max(r, b)), b, min(a, alpha))
    bounds = icon.getchannel("A").getbbox()
    if bounds:
        icon = icon.crop(bounds)
    canvas = Image.new("RGBA", (512, 512))
    icon.thumbnail((460, 460), Image.Resampling.LANCZOS)
    canvas.alpha_composite(icon, ((512-icon.width)//2, (512-icon.height)//2))
    canvas.save(OUTPUT / f"{name}.png", optimize=True)
