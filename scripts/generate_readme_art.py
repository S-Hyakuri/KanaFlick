"""Export README SVGs from the app's existing native brand paths.

No bitmap editing, new artwork, or font tracing. Run with Python 3.
"""
from html import escape
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
STARTUP = ROOT / "app/src/main/java/com/hyakuri/kanaflick/ui/startup"
OUT = ROOT / "docs/assets"


def generate(dark: bool) -> str:
    wordmarks = re.findall(r'Glyph\("([^"]+)"', (STARTUP / "WordmarkPaths.kt").read_text(encoding="utf-8"))
    logo = (STARTUP / "LogoDrawable.kt").read_text(encoding="utf-8")
    parts = re.findall(r'Part\("([^"]+)", "(#[A-Fa-f0-9]+)", "(#[A-Fa-f0-9]+)"\)', logo)
    ribbon = (STARTUP / "RibbonArtwork.kt").read_text(encoding="utf-8")
    ribbons = re.findall(r'val (rear|front) = PathParser.createPathFromPathData\("([^"]+)"\)', ribbon)
    assert wordmarks and len(parts) == 7 and len(ribbons) == 2
    bg, endbg, edge, ink, muted, chip = (
        ("#0D1627", "#142D48", "#22334D", "#EDF5FF", "#A6BCD5", "#182B43") if dark else
        ("#F3F8FF", "#E7F4FF", "#DCEBFA", "#203D60", "#526D8C", "#E4F1FF")
    )
    defs = [
        f'<linearGradient id="bg" x2="1" y2="1"><stop stop-color="{bg}"/><stop offset="1" stop-color="{endbg}"/></linearGradient>',
        '<linearGradient id="word" gradientUnits="userSpaceOnUse" x1="170" y1="240" x2="650" y2="90"><stop stop-color="#087BFF"/><stop offset="1" stop-color="#19B6F8"/></linearGradient>',
        '<linearGradient id="ribbon"><stop stop-color="#54B9F3"/><stop offset=".47" stop-color="#BDE7FC"/><stop offset="1" stop-color="#30B4F1"/></linearGradient>',
    ]
    for i, (_, start, end) in enumerate(parts):
        defs.append(f'<linearGradient id="p{i}" x2="1" y2="1"><stop stop-color="{start}"/><stop offset="1" stop-color="{end}"/></linearGradient>')

    def paths(ds, fill):
        return "".join(f'<path d="{escape(d)}" fill="{fill}"/>' for d in ds)

    # Keep the app's drawing order: rear ribbon, tiles, front ribbon, signals.
    logo_paths = paths([ribbons[0][1]], "url(#ribbon)")
    logo_paths += "".join(paths([p[0]], f"url(#p{i})") for i, p in enumerate(parts[:4]))
    logo_paths += paths([ribbons[1][1]], "url(#ribbon)")
    logo_paths += "".join(paths([p[0]], f"url(#p{i})") for i, p in enumerate(parts[4:], start=4))
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="1280" height="440" viewBox="0 0 1280 440" role="img" aria-labelledby="title desc">
<title id="title">KanaFlick</title>
<desc id="desc">Japanese typing practice. Original KanaFlick logo and wordmark on a {"dark" if dark else "light"} blue background.</desc>
<defs>{"".join(defs)}</defs>
<rect x="1" y="1" width="1278" height="438" rx="32" fill="url(#bg)" stroke="{edge}" stroke-width="2"/>
<circle cx="1080" cy="214" r="167" fill="{chip}"/>
<circle cx="1080" cy="214" r="190" fill="none" stroke="{edge}"/>
<g font-family="Segoe UI,Arial,sans-serif">
<text x="72" y="68" fill="{muted}" font-size="14" letter-spacing="3.5">JAPANESE TYPING PRACTICE</text>
<g transform="translate(40 100) scale(.92)">{paths(wordmarks, "url(#word)")}</g>
<text x="74" y="342" fill="{ink}" font-size="22" font-family="Yu Gothic,Meiryo,sans-serif">フリックで、ことばが広がる。</text>
<text x="76" y="379" fill="{muted}" font-size="15" letter-spacing="2">WORDS / SENTENCES / KANA</text>
</g>
<g transform="translate(870 36) scale(.265)">{logo_paths}</g>
</svg>
'''


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    for dark in (False, True):
        target = OUT / f'banner-{"dark" if dark else "light"}.svg'
        target.write_text(generate(dark), encoding="utf-8")
        print(target.relative_to(ROOT))
    version = re.search(r'versionName\s*=\s*"([^"]+)"', (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")).group(1)
    badges = [
        ("android", "Android", "8.0+", 64, 46, "#277A52"),
        ("material", "Material", "3", 68, 28, "#087FCC"),
        ("version", "Development", version, 94, 54, "#087FCC"),
        ("star", "Star", "on GitHub", 42, 76, "#4B6381"),
    ]
    for name, left, right, lw, rw, color in badges:
        width = lw + rw
        svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="24" role="img" aria-label="{escape(left)}: {escape(right)}">
<title>{escape(left)}: {escape(right)}</title>
<rect width="{width}" height="24" rx="5" fill="{color}"/>
<path d="M5 0H{lw}V24H5Q0 24 0 19V5Q0 0 5 0" fill="#364354"/>
<g fill="white" text-anchor="middle" font-family="Segoe UI,Arial,sans-serif" font-size="11"><text x="{lw / 2}" y="16">{escape(left)}</text><text x="{lw + rw / 2}" y="16">{escape(right)}</text></g>
</svg>
'''
        (OUT / f"badge-{name}.svg").write_text(svg, encoding="utf-8")
