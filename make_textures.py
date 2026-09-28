import os
from PIL import Image, ImageDraw

def create_slot(draw, x, y, size=18, is_big=False):
    w = size
    # Slot background (darker inset)
    draw.rectangle([x, y, x + w - 1, y + w - 1], fill=(11, 15, 25, 230))
    # Inset shadow (top & left darker, bottom & right subtle light)
    draw.line([(x, y), (x + w - 1, y)], fill=(6, 9, 15, 255))
    draw.line([(x, y), (x, y + w - 1)], fill=(6, 9, 15, 255))
    draw.line([(x, y + w - 1), (x + w - 1, y + w - 1)], fill=(30, 41, 59, 200))
    draw.line([(x + w - 1, y), (x + w - 1, y + w - 1)], fill=(30, 41, 59, 200))

def create_inventory():
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # 176x166 Main Body
    draw.rounded_rectangle([0, 0, 175, 165], radius=6, fill=(15, 23, 42, 245), outline=(51, 65, 85, 255), width=1)
    # Header Accent bar
    draw.rounded_rectangle([0, 0, 175, 5], radius=2, fill=(99, 102, 241, 255))

    # Player preview box
    draw.rectangle([25, 7, 75, 77], fill=(10, 14, 26, 220), outline=(30, 41, 59, 255))

    # Armor slots (4 vertical)
    for i in range(4):
        create_slot(draw, 7, 7 + i * 18)

    # Offhand slot
    create_slot(draw, 76, 61)

    # 2x2 Crafting slots
    create_slot(draw, 97, 17)
    create_slot(draw, 115, 17)
    create_slot(draw, 97, 35)
    create_slot(draw, 115, 35)

    # Crafting result slot
    create_slot(draw, 153, 27, size=20)

    # Arrow indicator
    draw.polygon([(137, 34), (145, 34), (145, 31), (150, 36), (145, 41), (145, 38), (137, 38)], fill=(99, 102, 241, 220))

    # 3x9 Main Inventory
    for row in range(3):
        for col in range(9):
            create_slot(draw, 7 + col * 18, 83 + row * 18)

    # 1x9 Hotbar
    for col in range(9):
        create_slot(draw, 7 + col * 18, 141)

    return im

def create_chest():
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # 176x222 Main Body for double chest
    draw.rounded_rectangle([0, 0, 175, 221], radius=6, fill=(15, 23, 42, 245), outline=(51, 65, 85, 255), width=1)
    draw.rounded_rectangle([0, 0, 175, 5], radius=2, fill=(99, 102, 241, 255))

    # Chest 6 rows
    for row in range(6):
        for col in range(9):
            create_slot(draw, 7 + col * 18, 17 + row * 18)

    # Player 3 rows
    for row in range(3):
        for col in range(9):
            create_slot(draw, 7 + col * 18, 139 + row * 18)

    # Hotbar
    for col in range(9):
        create_slot(draw, 7 + col * 18, 197)

    return im

def create_crafting_table():
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    draw.rounded_rectangle([0, 0, 175, 165], radius=6, fill=(15, 23, 42, 245), outline=(51, 65, 85, 255), width=1)
    draw.rounded_rectangle([0, 0, 175, 5], radius=2, fill=(99, 102, 241, 255))

    # 3x3 Crafting Grid
    for row in range(3):
        for col in range(3):
            create_slot(draw, 29 + col * 18, 16 + row * 18)

    # Result
    create_slot(draw, 123, 34, size=22)

    # Arrow
    draw.polygon([(89, 41), (105, 41), (105, 37), (113, 44), (105, 51), (105, 47), (89, 47)], fill=(99, 102, 241, 220))

    # Player inventory
    for row in range(3):
        for col in range(9):
            create_slot(draw, 7 + col * 18, 83 + row * 18)

    # Hotbar
    for col in range(9):
        create_slot(draw, 7 + col * 18, 141)

    return im

def create_widgets():
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # Hotbar (182 x 22 at 0, 0)
    draw.rounded_rectangle([0, 0, 181, 21], radius=3, fill=(15, 23, 42, 230), outline=(51, 65, 85, 255), width=1)
    for i in range(1, 9):
        draw.line([(i * 20, 1), (i * 20, 20)], fill=(30, 41, 59, 200))

    # Hotbar selection box (24x24 at 0, 22)
    draw.rounded_rectangle([0, 22, 23, 45], radius=3, fill=(99, 102, 241, 40), outline=(99, 102, 241, 255), width=2)

    # Button disabled (200x20 at 0, 46)
    draw.rounded_rectangle([0, 46, 199, 65], radius=4, fill=(11, 15, 25, 180), outline=(30, 41, 59, 200), width=1)

    # Button normal (200x20 at 0, 66)
    draw.rounded_rectangle([0, 66, 199, 85], radius=4, fill=(15, 23, 42, 240), outline=(51, 65, 85, 255), width=1)
    draw.line([(1, 67), (198, 67)], fill=(71, 85, 105, 150))

    # Button hovered (200x20 at 0, 86)
    draw.rounded_rectangle([0, 86, 199, 105], radius=4, fill=(30, 27, 75, 250), outline=(99, 102, 241, 255), width=1)
    draw.line([(1, 87), (198, 87)], fill=(129, 140, 248, 200))

    return im

def create_options_bg():
    im = Image.new('RGBA', (32, 32), (11, 15, 25, 255))
    draw = ImageDraw.Draw(im)
    for y in range(0, 32, 4):
        for x in range(0, 32, 4):
            if (x + y) % 8 == 0:
                draw.rectangle([x, y, x + 3, y + 3], fill=(15, 23, 42, 255))
    return im

base_dir = r"src/main/resources/resourcepacks/astra_pvp/assets/minecraft/textures/gui"
os.makedirs(os.path.join(base_dir, "container"), exist_ok=True)

create_inventory().save(os.path.join(base_dir, "container", "inventory.png"))
create_chest().save(os.path.join(base_dir, "container", "generic_54.png"))
create_crafting_table().save(os.path.join(base_dir, "container", "crafting_table.png"))
create_widgets().save(os.path.join(base_dir, "widgets.png"))
create_options_bg().save(os.path.join(base_dir, "options_background.png"))
print("Generated custom GUI textures successfully!")
