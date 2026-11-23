from PIL import Image, ImageDraw
import math
import os

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
TEAL = (15, 118, 110, 255)  # #0F766E
WHITE = (255, 255, 255, 255)


def draw_gauge(draw, cx, cy, outer_r, thickness):
    """Classic meter: arc open at the bottom so the glyph stays visually centered."""
    bbox_outer = [cx - outer_r, cy - outer_r, cx + outer_r, cy + outer_r]
    # Pillow ImageDraw: 0° = 3 o'clock, angles increase CLOCKWISE.
    # 90° = bottom, 180° = left, 270° = top.
    gap = 72  # free-space opening centered on the bottom
    start = 90 + gap / 2  # bottom-left tip
    end = 90 - gap / 2 + 360  # bottom-right tip after sweeping through top
    draw.arc(bbox_outer, start=start, end=end, fill=WHITE, width=thickness)

    # Needle toward upper-right (math angle: 0°=right, CCW; screen Y grows down)
    needle_len = outer_r - thickness * 0.28
    math_ang = math.radians(48)
    nx = cx + needle_len * math.cos(math_ang)
    ny = cy - needle_len * math.sin(math_ang)
    hub = max(2, int(thickness * 0.35))
    draw.ellipse([cx - hub, cy - hub, cx + hub, cy + hub], fill=WHITE)
    draw.line([(cx, cy), (nx, ny)], fill=WHITE, width=max(3, thickness // 3))



def _paint_gauge(size: int) -> Image.Image:
    """Full-bleed teal square with gauge glyph (used before masking)."""
    img = Image.new("RGBA", (size, size), TEAL)
    draw = ImageDraw.Draw(img)
    cx = size / 2
    # Nudge down so a bottom gap doesn't make the glyph look high.
    cy = size / 2 + size * 0.045
    outer = size * 0.30
    thickness = max(4, int(size * 0.08))
    draw_gauge(draw, cx, cy, outer, thickness)
    return img


def make_full(size: int) -> Image.Image:
    """Rounded-square launcher icon with transparent outside corners."""
    full = _paint_gauge(size)
    radius = max(8, int(size * 0.22))
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(full, mask=mask)
    return out


def make_fg(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cx = size / 2
    cy = size / 2 + size * 0.045
    outer = size * 0.26
    thickness = max(4, int(size * 0.07))
    draw_gauge(draw, cx, cy, outer, thickness)
    return img


def make_bg(size: int) -> Image.Image:
    return Image.new("RGBA", (size, size), TEAL)


def make_round(size: int) -> Image.Image:
    """Circular launcher icon with transparent corners."""
    full = _paint_gauge(size)
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse([0, 0, size - 1, size - 1], fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(full, mask=mask)
    return out


def save_webp(img: Image.Image, path: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, "WEBP", quality=95, method=6)


def main() -> None:
    full_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    layer_sizes = {
        "mipmap-mdpi": 108,
        "mipmap-hdpi": 162,
        "mipmap-xhdpi": 216,
        "mipmap-xxhdpi": 324,
        "mipmap-xxxhdpi": 432,
    }

    res = os.path.join(ROOT, "app", "src", "main", "res")
    for folder, size in full_sizes.items():
        full = make_full(size)
        save_webp(full, os.path.join(res, folder, "ic_launcher.webp"))
        save_webp(make_round(size), os.path.join(res, folder, "ic_launcher_round.webp"))

    for folder, size in layer_sizes.items():
        save_webp(make_fg(size), os.path.join(res, folder, "ic_launcher_foreground.webp"))
        save_webp(make_bg(size), os.path.join(res, folder, "ic_launcher_background.webp"))

    assets = os.path.join(ROOT, "assets")
    os.makedirs(assets, exist_ok=True)
    logo = make_full(1024)
    logo.save(os.path.join(assets, "logo.png"), "PNG")
    logo.save(os.path.join(assets, "logo-rounded.png"), "PNG")
    logo.save(os.path.join(assets, "icon-1024.png"), "PNG")
    make_round(1024).save(os.path.join(assets, "logo-round.png"), "PNG")
    print("generated mipmaps + assets")


if __name__ == "__main__":
    main()
