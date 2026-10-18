#!/usr/bin/env python3
"""
Silah modelleri uretici.

Calistir:   python3 tools/gen_models.py
Uretir:     assets/silahmod/models/item/*.json   (3D modeller + reload kareleri)
            assets/silahmod/items/*.json         (hangi kare ne zaman gosterilir)
            assets/silahmod/textures/item/palette.png

Silahlar kucuk kutulardan (cuboid) olusur. Hepsi ayni 16x16'lik renk paletini kullanir,
yani ekstra doku cizmen gerekmez.

KOORDINAT MANTIGI (silah-yerel):
    l = yanlamasina (- sol, oyuncunun gordugu taraf)
    y = yukseklik
    u = ileri (namlu yonu). Minecraft'ta namlu KUZEYE (-Z) bakar.

RELOAD KARELERI:
    Her silahin FRAMES listesi var. Kare sayisini degistirirsen WeaponConfig'deki
    *_RELOAD_FRAMES sabitini de ayni sayiya getir.
"""
import json
import math
import os
import struct
import zlib

import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.normpath(os.path.join(HERE, "..", "src", "main", "resources", "assets", "silahmod"))
EMPTY_FRAME = 99  # mermisi bitmis RPG (roketsiz) modeli icin ozel kare numarasi

# --------------------------------------------------------------------------- palet
PALETTE = [
    ("gunmetal", "#2f3237"), ("steel_dark", "#1c1e22"), ("steel", "#6b7078"), ("steel_light", "#aab0b8"),
    ("wood", "#6e4a2a"), ("wood_dark", "#4a301b"), ("brass", "#d4a82a"), ("olive", "#4d5535"),
    ("olive_dark", "#363c25"), ("black", "#0d0d0f"), ("rocket_grey", "#7d838b"), ("rocket_red", "#b8391c"),
    ("lens", "#2f5d86"), ("leather", "#3a281d"), ("tube", "#58603f"), ("white", "#e8e8e8"),
    # --- zirhlar (tools/gen_armor.py kullanir) ---
    ("helm_green", "#1f5a46"), ("helm_green_dark", "#14382c"), ("helm_dark", "#3a3d3b"),
    ("helm_rail", "#5a5f5c"), ("visor", "#0f1318"), ("strap_black", "#1a1b1c"),
    ("vest_grey", "#8f9389"), ("vest_grey_dark", "#5f645e"), ("vest_grey_light", "#b0b4aa"),
    ("vest_brown", "#76633f"), ("vest_brown_dark", "#4a3d27"), ("vest_brown_light", "#97835a"),
    # --- medkit ---
    ("kit_red", "#d92a1e"), ("kit_white", "#eceff1"),
]
IDX = {name: i for i, (name, _) in enumerate(PALETTE)}


def hex_rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


def write_palette_png(path):
    w = h = 16
    rows = []
    for y in range(h):
        row = bytearray([0])
        for x in range(w):
            i = y * w + x
            r, g, b = hex_rgb(PALETTE[i][1]) if i < len(PALETTE) else (255, 0, 255)
            row += bytes((r, g, b, 255))
        rows.append(bytes(row))
    raw = b"".join(rows)

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)) \
        + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


# --------------------------------------------------------------------------- kutu yardimcilari
def G(color, l1, l2, y1, y2, u1, u2):
    """Silah-yerel koordinatla kutu. Donus: model uzayinda kutu."""
    return {"c": color, "f": (8 + l1, y1, 8 - u2), "t": (8 + l2, y2, 8 - u1)}


def sh(boxes, dl=0.0, dy=0.0, du=0.0):
    """Kutulari kaydir (animasyon icin)."""
    return [{"c": b["c"],
             "f": (b["f"][0] + dl, b["f"][1] + dy, b["f"][2] - du),
             "t": (b["t"][0] + dl, b["t"][1] + dy, b["t"][2] - du)} for b in boxes]


# --------------------------------------------------------------------------- REVOLVER
def revolver(st):
    b = []
    # govde
    b += [G("gunmetal", -0.9, 0.9, 8.3, 11.2, -3.5, 0.5)]
    b += [G("gunmetal", -0.8, 0.8, 11.2, 12.0, -3.5, 1.0)]            # ust kayis
    b += [G("gunmetal", -0.9, 0.9, 7.5, 8.3, -3.0, 0.5)]              # alt govde
    # namlu
    b += [G("steel_dark", -0.7, 0.7, 9.8, 11.4, 0.5, 9.5)]
    b += [G("steel", -0.4, 0.4, 11.4, 11.9, 1.0, 9.5)]                # nisan rayi
    b += [G("black", -0.25, 0.25, 11.9, 12.5, 8.8, 9.5)]              # on arpacik
    b += [G("black", -0.5, 0.5, 12.0, 12.6, -3.5, -2.8)]              # arka nisangah
    b += [G("gunmetal", -0.4, 0.4, 8.6, 9.8, 1.0, 7.5)]               # bosaltma cubugu
    # horoz
    b += [G("steel_dark", -0.35, 0.35, 10.8, 12.4, -4.3, -3.5)]
    b += [G("steel_dark", -0.35, 0.35, 12.0, 12.8, -4.7, -3.9)]
    # tetik koruma + tetik
    b += [G("steel_dark", -0.4, 0.4, 6.9, 7.5, -2.6, 1.6)]
    b += [G("steel_dark", -0.4, 0.4, 6.9, 8.6, 1.1, 1.6)]
    b += [G("black", -0.3, 0.3, 7.5, 9.0, -0.7, -0.3)]
    # kabza (basamakli, geriye egimli)
    b += [G("wood", -0.95, 0.95, 5.9, 8.3, -3.6, -1.6)]
    b += [G("wood", -0.95, 0.95, 4.3, 5.9, -4.0, -2.0)]
    b += [G("wood", -0.95, 0.95, 2.7, 4.3, -4.4, -2.4)]
    b += [G("steel_dark", -0.95, 0.95, 2.0, 2.7, -4.6, -2.3)]         # kabza kapagi
    b += [G("wood_dark", -0.5, 0.5, 8.3, 11.0, -4.3, -3.5)]           # arka kayis

    # silindir (sola, yani oyuncunun gordugu tarafa acilir)
    out = st.get("out", 0.0)
    cyl = [G("steel", -1.35, 1.35, 9.1, 11.1, -1.4, 1.9),
           G("steel_dark", -1.0, 1.0, 8.7, 11.5, -1.4, 1.9),
           G("black", -0.3, 0.3, 10.0, 10.4, -1.45, -1.4)]
    b += sh(cyl, dl=-out)
    if out > 0.01:
        b += [G("steel", -out, -0.3, 8.9, 9.5, -0.7, 0.7)]            # silindir kolu

    # bosalan kovanlar (arkaya, yani asagi dogru kayar)
    rounds = st.get("rounds")
    if rounds is not None:
        for k in range(6):
            a = math.radians(60 * k)
            cl = -out + 0.8 * math.cos(a)
            cy = 10.1 + 0.8 * math.sin(a)
            b += [G("brass", cl - 0.3, cl + 0.3, cy - 0.3 - rounds * 0.9, cy + 0.3 - rounds * 0.9, -1.6 - rounds, 0.3 - rounds)]

    # hizli doldurucu (speedloader) arkadan silindire girer
    gap = st.get("loader")
    if gap is not None:
        b += [G("steel", -1.1, 1.1, 8.9, 11.3, -2.6 - gap, -1.9 - gap)]
        for k in range(6):
            a = math.radians(60 * k)
            cl = -out + 0.8 * math.cos(a)
            cy = 10.1 + 0.8 * math.sin(a)
            b += [G("brass", cl - 0.3, cl + 0.3, cy - 0.3, cy + 0.3, -1.9 - gap, -0.3 - gap)]
    return b


REVOLVER_FRAMES = [
    dict(dr=(5, 0, -5)),
    dict(out=2.0, dr=(9, 0, -10)),
    dict(out=3.6, rounds=1.5, dr=(14, 1, -14)),
    dict(out=3.6, rounds=5.5, dr=(18, 2, -16)),
    dict(out=3.6, dr=(14, 2, -15)),
    dict(out=3.6, loader=3.5, dr=(-8, 0, -14)),
    dict(out=3.6, loader=1.2, dr=(-8, 0, -12)),
    dict(out=1.6, dr=(0, 0, -6)),
    dict(out=0.0, dr=(3, 0, -3)),
]


# --------------------------------------------------------------------------- KESKIN NISANCI
def sniper(st):
    b = []
    # dipcik
    b += [G("steel_dark", -0.9, 0.9, 3.9, 8.7, -10.6, -10.1)]         # dipcik pedi
    b += [G("olive", -0.85, 0.85, 4.4, 8.4, -10.1, -3.0)]
    b += [G("olive_dark", -0.8, 0.8, 8.4, 9.4, -8.2, -4.6)]           # yanak dayama
    b += [G("olive", -0.85, 0.85, 2.6, 5.6, -4.4, -3.0)]              # tabanca kabzasi
    b += [G("olive", -0.85, 0.85, 3.4, 6.4, -3.6, -2.4)]
    # govde (receiver)
    b += [G("gunmetal", -0.95, 0.95, 7.0, 10.0, -3.0, 3.2)]
    b += [G("steel_dark", -0.97, 0.97, 7.4, 7.8, -2.8, 3.0)]          # panel cizgisi
    # on kundak
    b += [G("olive", -0.8, 0.8, 6.6, 8.8, 3.2, 9.0)]
    b += [G("olive_dark", -0.82, 0.82, 6.4, 6.6, 3.2, 9.0)]
    # namlu + susturucu/agiz freni
    b += [G("steel_dark", -0.5, 0.5, 8.0, 9.4, 9.0, 17.0)]
    b += [G("gunmetal", -0.55, 0.55, 8.1, 9.3, 3.2, 9.0)]
    b += [G("black", -0.75, 0.75, 7.85, 9.55, 17.0, 19.2)]
    b += [G("steel_dark", -0.77, 0.77, 8.3, 8.6, 17.4, 17.7), G("steel_dark", -0.77, 0.77, 8.3, 8.6, 18.2, 18.5)]
    # durbun
    b += [G("black", -0.7, 0.7, 10.8, 12.2, -2.6, 4.2)]
    b += [G("black", -0.92, 0.92, 10.6, 12.4, 3.2, 5.0)]              # on kafa
    b += [G("lens", -0.6, 0.6, 10.9, 12.1, 5.0, 5.15)]                # on mercek
    b += [G("black", -0.85, 0.85, 10.65, 12.35, -3.6, -2.6)]          # goz kismi
    b += [G("steel_dark", -0.5, 0.5, 10.0, 10.8, -1.8, -1.0), G("steel_dark", -0.5, 0.5, 10.0, 10.8, 1.6, 2.4)]
    b += [G("steel", -0.35, 0.35, 12.2, 12.9, 0.1, 0.9)]              # ayar topuzu
    # tetik koruma + tetik
    b += [G("steel_dark", -0.35, 0.35, 5.9, 6.4, -2.8, -0.2), G("steel_dark", -0.35, 0.35, 5.9, 7.0, -0.5, -0.2)]
    b += [G("black", -0.25, 0.25, 6.4, 7.0, -1.6, -1.3)]

    # sarjor
    mag_dy = st.get("mag_dy", 0.0)
    if mag_dy is not None:
        b += sh([G("steel_dark", -0.7, 0.7, 4.6, 7.4, -0.4, 2.6),
                 G("brass", -0.4, 0.4, 7.4, 7.9, 0.4, 1.6)], dy=mag_dy)

    # sürgü (bolt) - gorulen tarafa koyduk ki animasyon gorunsun
    du = st.get("bolt_du", 0.0)
    bolt = [G("steel", -0.45, 0.45, 8.9, 9.8, -2.6, -0.2)]
    if st.get("handle_up"):
        bolt += [G("steel_light", -1.5, -0.9, 8.9, 11.0, -2.2, -1.6),
                 G("steel_light", -1.9, -0.9, 11.0, 11.9, -2.5, -1.3)]
    else:
        bolt += [G("steel_light", -2.6, -0.9, 8.6, 9.2, -2.2, -1.6),
                 G("steel_light", -3.4, -2.6, 8.3, 9.5, -2.5, -1.3)]
    b += sh(bolt, du=du)
    return b


SNIPER_FRAMES = [
    dict(handle_up=True, dr=(2, 0, -4)),
    dict(handle_up=True, bolt_du=-1.3, dr=(2, 0, -4)),
    dict(handle_up=True, bolt_du=-2.6, dr=(3, 0, -6)),
    dict(handle_up=True, bolt_du=-2.6, mag_dy=-2.0, dr=(4, 0, -8)),
    dict(handle_up=True, bolt_du=-2.6, mag_dy=-6.5, dr=(6, 0, -12)),
    dict(handle_up=True, bolt_du=-2.6, mag_dy=None, dr=(6, 0, -12)),
    dict(handle_up=True, bolt_du=-2.6, mag_dy=-6.5, dr=(4, 0, -10)),
    dict(handle_up=True, bolt_du=-2.6, mag_dy=-2.2, dr=(3, 0, -8)),
    dict(handle_up=True, bolt_du=-1.3, mag_dy=0.0, dr=(2, 0, -5)),
    dict(handle_up=False, bolt_du=0.0, dr=(1, 0, -2)),
]


# --------------------------------------------------------------------------- RPG
RPG_BACK_SHIFT = -6.0


def rpg(st):
    b = []
    # tup (sekizgen kesit)
    b += [G("tube", -1.0, 1.0, 8.0, 10.0, -6.0, 9.0), G("tube", -0.75, 0.75, 7.75, 10.25, -6.0, 9.0)]
    # arka huni (venturi)
    b += [G("steel_dark", -1.35, 1.35, 8.0, 10.0, -9.2, -6.0), G("steel_dark", -1.05, 1.05, 7.65, 10.35, -9.2, -6.0)]
    # ahsap isi kalkani
    b += [G("wood", -1.25, 1.25, 8.0, 10.0, 0.0, 4.5), G("wood", -1.0, 1.0, 7.75, 10.25, 0.0, 4.5)]
    b += [G("wood_dark", -1.27, 1.27, 8.8, 9.2, 0.0, 4.5)]
    # on agiz
    b += [G("steel_dark", -1.2, 1.2, 8.0, 10.0, 9.0, 11.0), G("steel_dark", -0.95, 0.95, 7.8, 10.2, 9.0, 11.0)]
    # kabza (basamakli) + tetik koruma
    b += [G("wood", -0.6, 0.6, 5.6, 7.75, 1.6, 3.6), G("wood", -0.6, 0.6, 4.0, 5.6, 1.2, 3.2)]
    b += [G("steel_dark", -0.3, 0.3, 5.0, 5.4, 3.6, 5.2), G("steel_dark", -0.3, 0.3, 5.0, 7.75, 5.0, 5.2)]
    b += [G("black", -0.2, 0.2, 6.2, 7.75, 3.8, 4.1)]
    # on tutamak
    b += [G("wood", -0.5, 0.5, 5.4, 7.75, 6.4, 7.6)]
    # arka/on nisan
    b += [G("black", -0.15, 0.15, 10.25, 11.1, -3.0, -2.6), G("black", -0.15, 0.15, 10.25, 11.0, 7.6, 8.0)]
    # optik nisangah (sol yan, oyuncunun gordugu taraf)
    b += [G("steel_dark", -1.9, -1.0, 8.5, 9.7, 0.6, 3.6), G("lens", -1.92, -1.9, 8.7, 9.5, 3.6, 3.7),
          G("steel_dark", -1.1, -0.9, 8.9, 9.3, 0.8, 3.2)]

    # roket
    r = st.get("rocket", (0, 0))  # varsayilan: dolu (roket takili)
    if r is not None:
        rdu, rdy = r
        rocket = [G("gunmetal", -0.45, 0.45, 8.55, 9.45, 9.5, 11.0),    # tup icindeki sap
                  G("rocket_grey", -1.4, 1.4, 7.6, 10.4, 11.0, 14.0),   # sisman govde
                  G("steel_dark", -1.42, 1.42, 8.3, 8.7, 11.6, 12.0),
                  G("rocket_red", -1.05, 1.05, 8.0, 10.0, 14.0, 16.4),
                  G("rocket_red", -0.55, 0.55, 8.5, 9.5, 16.4, 17.8)]
        b += sh(rocket, dy=rdy, du=rdu)
    # Tum modeli geriye al: modeller -16..32 araliginda kalmali, roket one dogru uzaklasiyor.
    return sh(b, du=RPG_BACK_SHIFT)


RPG_FRAMES = [
    dict(rocket=(0, 0), dr=(-12, 0, -6)),
    dict(rocket=(3.5, 0), dr=(-22, 0, -10)),
    dict(rocket=(9, -4), dr=(-30, 0, -12)),
    dict(rocket=None, dr=(-30, 0, -12)),
    dict(rocket=(11.5, 3), dr=(-30, 0, -12)),
    dict(rocket=(8, 2.2), dr=(-28, 0, -10)),
    dict(rocket=(4.5, 1.2), dr=(-24, 0, -8)),
    dict(rocket=(1.8, 0.4), dr=(-18, 0, -6)),
    dict(rocket=(0, 0), dr=(-10, 0, -4)),
    dict(rocket=(0, 0), dr=(-3, 0, -1)),
]
RPG_EMPTY = dict(rocket=None, dr=(0, 0, 0))


# --------------------------------------------------------------------------- BICAK
def knife(st):
    b = []
    b += [G("steel_light", -0.3, 0.3, 9.0, 10.2, 1.6, 8.6)]           # sirt
    b += [G("steel", -0.31, 0.31, 8.5, 9.0, 1.9, 8.2)]                # kesici kenar
    b += [G("steel_light", -0.3, 0.3, 9.4, 10.2, 8.6, 10.2)]          # uc (basamakli)
    b += [G("steel_light", -0.3, 0.3, 9.8, 10.2, 10.2, 11.2)]
    b += [G("steel_dark", -0.9, 0.9, 8.2, 10.4, 1.0, 1.6)]            # siper
    b += [G("leather", -0.6, 0.6, 8.0, 10.1, -4.0, 1.0)]              # sap
    for a, c in ((-3.4, -3.0), (-2.2, -1.8), (-1.0, -0.6)):
        b += [G("black", -0.68, 0.68, 7.9, 10.2, a, c)]               # tutus halkalari
    b += [G("steel_dark", -0.7, 0.7, 7.7, 10.4, -4.6, -4.0)]          # pomel
    return b


# --------------------------------------------------------------------------- ekran (display) matematigi
def rot_xyz(rx, ry, rz):
    """Minecraft: vertex once Z, sonra Y, sonra X ile doner (Rx @ Ry @ Rz)."""
    a, b, c = map(math.radians, (rx, ry, rz))
    Rx = np.array([[1, 0, 0], [0, math.cos(a), -math.sin(a)], [0, math.sin(a), math.cos(a)]])
    Ry = np.array([[math.cos(b), 0, math.sin(b)], [0, 1, 0], [-math.sin(b), 0, math.cos(b)]])
    Rz = np.array([[math.cos(c), -math.sin(c), 0], [math.sin(c), math.cos(c), 0], [0, 0, 1]])
    return Rx @ Ry @ Rz


CENTER = np.array([8.0, 8.0, 8.0])


def bbox_center(boxes):
    lo = np.min([b["f"] for b in boxes], axis=0)
    hi = np.max([b["t"] for b in boxes], axis=0)
    return (lo + hi) / 2, hi - lo


def disp(rot, scale, pivot, hand):
    """Pivot noktasi (tutus yeri) elin oldugu noktaya (hand) gelsin diye ceviri hesapla."""
    R = rot_xyz(*rot)
    t = np.array(hand) - scale * (R @ (np.array(pivot) - CENTER))
    return {"rotation": [round(float(v), 2) for v in rot],
            "translation": [round(float(v), 2) for v in t],
            "scale": [scale, scale, scale]}


def disp_centered(rot, scale, boxes):
    """Modelin ortasi merkeze gelsin (gui / yer / cerceve icin)."""
    R = rot_xyz(*rot)
    c, _ = bbox_center(boxes)
    t = -scale * (R @ (c - CENTER))
    return {"rotation": list(rot),
            "translation": [round(float(v), 2) for v in t],
            "scale": [scale, scale, scale]}


def fit_scale(rot, boxes, target):
    """Dondurulmus modelin ekrandaki en buyuk boyutu 'target' birim olsun."""
    R = rot_xyz(*rot)
    pts = []
    for b in boxes:
        for x in (b["f"][0], b["t"][0]):
            for y in (b["f"][1], b["t"][1]):
                for z in (b["f"][2], b["t"][2]):
                    pts.append(R @ (np.array([x, y, z]) - CENTER))
    pts = np.array(pts)
    ext = max(np.ptp(pts[:, 0]), np.ptp(pts[:, 1]))
    return round(min(4.0, target / ext), 3)


# Her silah icin ayarlar.
#   pivot : tutus noktasi (model koordinati: x=8+l, y, z=8-u)
#   fp    : birinci sahis. rot=(pitch, yaw, roll), scale, hand=(x,y,z) el konumuna ek kaydirma
#   tp    : ucuncu sahis
WEAPONS = {
    "revolver": dict(build=revolver, frames=REVOLVER_FRAMES, empty=None, hide_scoped=False,
                     pivot=(8, 5.2, 11.2),
                     fp=dict(rot=(6, 20, -3), scale=1.05, hand=(0.0, 5.5, -2.0)),
                     tp=dict(rot=(90, 0, 0), scale=0.72, hand=(0, 1.5, 0))),
    "sniper": dict(build=sniper, frames=SNIPER_FRAMES, empty=None, hide_scoped=True,
                   pivot=(8, 5.8, 11.4),
                   fp=dict(rot=(4, 18, 0), scale=0.82, hand=(1.0, 5.5, -3.0)),
                   tp=dict(rot=(90, 0, 0), scale=0.58, hand=(0, 1.5, 0))),
    "rpg": dict(build=rpg, frames=RPG_FRAMES, empty=RPG_EMPTY, hide_scoped=False,
                pivot=(8, 6.2, 5.4 - RPG_BACK_SHIFT),
                fp=dict(rot=(4, 26, 0), scale=0.72, hand=(1.0, 5.0, -2.0)),
                tp=dict(rot=(90, 0, 0), scale=0.58, hand=(0, 1.5, 0))),
    "knife": dict(build=knife, frames=[], empty=None, hide_scoped=False,
                  pivot=(8, 9.0, 9.5),
                  fp=dict(rot=(30, 16, 0), scale=1.1, hand=(0.0, 5.0, -2.0)),
                  tp=dict(rot=(90, 0, 0), scale=0.85, hand=(0, 1.5, 0))),
}

GUI_ROT = (24, -52, 0)

# Birinci sahista silahi "geriye gom" (Elmas Kazma gibi sag-alta yaslansin, namlu ucu ekranin ortasina uzanmasin).
# Boyut (scale) degismez, sadece konum/aci kayar. Daha fazla gommek istersen sayilari buyut, azaltmak icin kucult.
#   shift = (saga, asagi(-), geriye/oyuncuya dogru)   pitch = namlu ucunu asagi egme (derece)   yaw = disa acma
FP_BACK = dict(shift=(6.0, -6.5, 4.0), pitch=28.0, yaw=8.0)


def build_display(name, cfg, st):
    boxes = cfg["build"](st)
    fp = cfg["fp"]
    dr = st.get("dr", (0, 0, 0))
    fp_rot = tuple(fp["rot"][i] + dr[i] for i in range(3))
    fp_rot = (fp_rot[0] + FP_BACK["pitch"], fp_rot[1] + FP_BACK["yaw"], fp_rot[2])
    fp_hand = tuple(fp["hand"][i] + FP_BACK["shift"][i] for i in range(3))
    fp_d = disp(fp_rot, fp["scale"], cfg["pivot"], fp_hand)
    tp = cfg["tp"]
    tp_d = disp(tp["rot"], tp["scale"], cfg["pivot"], tp["hand"])
    idle = cfg["build"]({})
    gs = fit_scale(GUI_ROT, idle, 14.5)
    gui = disp_centered(GUI_ROT, gs, idle)
    ground = disp_centered((0, 0, 0), 0.45, idle)
    fixed = disp_centered((0, 90, 0), min(1.0, fit_scale((0, 90, 0), idle, 13.0)), idle)
    return boxes, {
        "thirdperson_righthand": tp_d,
        "thirdperson_lefthand": tp_d,
        "firstperson_righthand": fp_d,
        "firstperson_lefthand": fp_d,
        "gui": gui,
        "ground": ground,
        "fixed": fixed,
        "head": disp_centered((0, 0, 0), 0.6, idle),
    }


# --------------------------------------------------------------------------- JSON cikti
def r1(v):
    return round(float(v), 3)


def element(b):
    k = IDX[b["c"]]
    px, py = k % 16, k // 16
    uv = [px + 0.3, py + 0.3, px + 0.7, py + 0.7]
    face = {"uv": uv, "texture": "#0"}
    return {
        "from": [r1(v) for v in b["f"]],
        "to": [r1(v) for v in b["t"]],
        "faces": {side: dict(face) for side in ("north", "south", "east", "west", "up", "down")},
    }


def model_json(boxes, display):
    return {
        "textures": {"0": "silahmod:item/palette", "particle": "silahmod:item/palette"},
        "texture_size": [16, 16],
        "elements": [element(b) for b in boxes],
        "display": display,
    }


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=1)
        f.write("\n")


def item_definition(name, n_frames, has_empty, hide_scoped):
    def model(n):
        return {"type": "minecraft:model", "model": f"silahmod:item/{n}"}

    if n_frames:
        entries = [{"threshold": float(i), "model": model(f"{name}_r{i}")} for i in range(1, n_frames + 1)]
        if has_empty:
            entries.append({"threshold": float(EMPTY_FRAME), "model": model(f"{name}_empty")})
        body = {"type": "minecraft:range_dispatch", "property": "minecraft:custom_model_data",
                "index": 0, "fallback": model(name), "entries": entries}
    else:
        body = model(name)

    if hide_scoped:
        # Scope acikken (sag tik basili) birinci sahista silahi gizle; scope katmani zaten ekrani kaplar.
        body = {
            "type": "minecraft:select", "property": "minecraft:display_context",
            "cases": [{"when": ["firstperson_righthand", "firstperson_lefthand"],
                       "model": {"type": "minecraft:condition", "property": "minecraft:using_item",
                                 "on_true": {"type": "minecraft:empty"}, "on_false": body}}],
            "fallback": body,
        }
    return {"model": body}


def main():
    write_palette_png(os.path.join(ASSETS, "textures", "item", "palette.png"))
    summary = []
    for name, cfg in WEAPONS.items():
        boxes, display = build_display(name, cfg, {})
        write_json(os.path.join(ASSETS, "models", "item", f"{name}.json"), model_json(boxes, display))
        for i, st in enumerate(cfg["frames"], start=1):
            boxes, display = build_display(name, cfg, st)
            write_json(os.path.join(ASSETS, "models", "item", f"{name}_r{i}.json"), model_json(boxes, display))
        if cfg["empty"] is not None:
            boxes, display = build_display(name, cfg, cfg["empty"])
            write_json(os.path.join(ASSETS, "models", "item", f"{name}_empty.json"), model_json(boxes, display))
        write_json(os.path.join(ASSETS, "items", f"{name}.json"),
                   item_definition(name, len(cfg["frames"]), cfg["empty"] is not None, cfg["hide_scoped"]))
        summary.append(f"{name}: {len(boxes)} parca, {len(cfg['frames'])} reload karesi")
    print("\n".join(summary))


if __name__ == "__main__":
    main()
