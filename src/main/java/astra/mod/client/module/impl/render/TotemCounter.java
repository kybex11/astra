package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TotemCounter extends Module {
    public static TotemCounter INSTANCE;

    public TotemCounter() {
        super("TotemCounter", "Количество тотемов в инвентаре", Category.HUD);
        INSTANCE = this;
        setEnabled(true);
    }

    public static int countTotems() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null) return 0;
            int count = 0;
            for (ItemStack stack : mc.player.inventory.items) {
                if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) count += stack.getCount();
            }
            for (ItemStack stack : mc.player.inventory.offhand) {
                if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) count += stack.getCount();
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
}
