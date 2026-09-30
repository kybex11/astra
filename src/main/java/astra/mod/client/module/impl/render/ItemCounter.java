package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.ModeSetting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemCounter extends Module {
    public static ItemCounter INSTANCE;

    public final ModeSetting item = new ModeSetting("Предмет", "Какой предмет считать",
            "Totem", "Totem", "Golden Apple", "Ender Pearl", "Arrow", "Obsidian", "Cobweb");

    public ItemCounter() {
        super("ItemCounter", "Счётчик выбранных предметов в инвентаре", Category.HUD);
        INSTANCE = this;
        addSettings(item);
        setEnabled(true);
    }

    private Item getTarget() {
        switch (item.getValue()) {
            case "Golden Apple": return Items.GOLDEN_APPLE;
            case "Ender Pearl": return Items.ENDER_PEARL;
            case "Arrow": return Items.ARROW;
            case "Obsidian": return Items.OBSIDIAN;
            case "Cobweb": return Items.COBWEB;
            case "Totem":
            default: return Items.TOTEM_OF_UNDYING;
        }
    }

    public int count() {
        try {
            if (mc.player == null) return 0;
            Item target = getTarget();
            int count = 0;
            for (ItemStack stack : mc.player.inventory.items) {
                if (!stack.isEmpty() && stack.getItem() == target) count += stack.getCount();
            }
            for (ItemStack stack : mc.player.inventory.offhand) {
                if (!stack.isEmpty() && stack.getItem() == target) count += stack.getCount();
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
}
