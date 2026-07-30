"""Recorta el atlas 4x4 generado y produce drawables PNG transparentes."""

from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "app/src/main/res/drawable-nodpi"

BUS_NAMES = [
    "ic_bus", "ic_pelota", "ic_manzana", "ic_osito",
    "ic_zapato", "ic_libro", "ic_flor", "ic_platano",
    "ic_gorra", "ic_cuchara", "ic_regalo", "ic_paraguas",
    "ic_gato", "ic_formas", "ic_estrellas", "ic_lapiz",
]

REWARD_NAMES = [
    "ic_perro", "ic_pajaro", "ic_pez",
    "ic_sol", "ic_nube", "ic_arcoiris",
    "ic_oso", "ic_album", "ic_estrella",
]


def recortar(source: Path, names: list[str], columnas: int) -> None:
    atlas = Image.open(source).convert("RGBA")
    ancho, alto = atlas.size
    for indice, nombre in enumerate(names):
        fila, columna = divmod(indice, columnas)
        caja = (
            round(columna * ancho / columnas),
            round(fila * alto / columnas),
            round((columna + 1) * ancho / columnas),
            round((fila + 1) * alto / columnas),
        )
        icono = atlas.crop(caja)
        alpha = icono.getchannel("A")
        contenido = alpha.getbbox()
        if contenido:
            icono = icono.crop(contenido)
        lado = max(icono.size)
        margen = max(12, lado // 12)
        lienzo = Image.new("RGBA", (lado + margen * 2, lado + margen * 2))
        lienzo.alpha_composite(
            icono,
            ((lienzo.width - icono.width) // 2, (lienzo.height - icono.height) // 2),
        )
        lienzo.thumbnail((256, 256), Image.Resampling.LANCZOS)
        lienzo.save(OUTPUT / f"{nombre}.png", optimize=True)


def main() -> None:
    fuentes = ROOT / "artwork/icon-atlases"
    recortar(fuentes / "icon_atlas_transparent.png", BUS_NAMES, 4)
    recortar(fuentes / "rewards_atlas_transparent.png", REWARD_NAMES, 3)


if __name__ == "__main__":
    main()
