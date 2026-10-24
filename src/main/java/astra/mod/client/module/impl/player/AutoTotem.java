package astra.mod.client.module.impl.player;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
    public static AutoTotem INSTANCE;

    public final NumberSetting healthThreshold = new NumberSetting("Health", "Порог HP для смены тотема", 12.0, 4.0, 18.0, 1.0);

    public AutoTotem() {
        super("AutoTotem", "Автоматически берет тотем бессмертия во вторую руку при низком HP", Category.PLAYER);
        INSTANCE = this;
        addSettings(healthThreshold);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;

        ItemStack offhand = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        if (mc.player.getHealth() <= healthThreshold.getValue()) {
            for (int i = 9; i < 45; i++) {
                ItemStack stack = mc.player.inventoryMenu.getSlot(i).getItem();
                if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    mc.gameMode.handleInventoryMouseClick(0, i, 0, ClickType.PICKUP, mc.player);
                    mc.gameMode.handleInventoryMouseClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.gameMode.handleInventoryMouseClick(0, i, 0, ClickType.PICKUP, mc.player);
                    break;
                }
            }
        }
    }
}
