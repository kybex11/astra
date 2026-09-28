package astra.mod.client.module.impl.player;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class GodMode extends Module {
    public static GodMode INSTANCE;

    public final BooleanSetting autoTotem = new BooleanSetting("AutoTotem", "Мгновенная подстановка тотема бессмертия", true);
    public final BooleanSetting serverNoFall = new BooleanSetting("ServerNoFall", "Пакетная нейтрализация урона от падения", true);
    public final BooleanSetting antiKnockback = new BooleanSetting("Anti-Knockback", "Отмена отбрасывания и анимации урона", true);
    public final BooleanSetting noFire = new BooleanSetting("NoFire", "Тушение огня и лавы", true);
    private long lastTotemSwap = 0L;
    private long lastNoFall = 0L;

    public GodMode() {
        super("GodMode", "Бессмертие — Smart AutoTotem, NoFall, отмена огня и урона", Category.PLAYER, GLFW.GLFW_KEY_G);
        INSTANCE = this;
        addSettings(autoTotem, serverNoFall, antiKnockback, noFire);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        long now = System.currentTimeMillis();

        // 1. Smart Auto-Totem (Rate-limited, only when offhand has no totem)
        if (autoTotem.getValue() && (now - lastTotemSwap > 400)) {
            if (mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                int totemSlot = findTotemSlot();
                if (totemSlot != -1 && mc.gameMode != null) {
                    mc.gameMode.handleInventoryMouseClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);
                    mc.gameMode.handleInventoryMouseClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.gameMode.handleInventoryMouseClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);
                    lastTotemSwap = now;
                }
            }
        }

        // 2. Server NoFall Packet Spoof (Rate limited)
        if (serverNoFall.getValue() && mc.player.fallDistance > 3.0f && mc.getConnection() != null && (now - lastNoFall > 600)) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY() + 0.001, mc.player.getZ(), true));
            mc.player.fallDistance = 0.0f;
            lastNoFall = now;
        }

        // 3. Cancel hurt time and knockback animation
        if (antiKnockback.getValue()) {
            mc.player.hurtTime = 0;
            mc.player.hurtDuration = 0;
        }

        // 4. Clear Fire & Drowning
        if (noFire.getValue()) {
            mc.player.clearFire();
            mc.player.setRemainingFireTicks(0);
            mc.player.setAirSupply(300);
        }
    }

    private int findTotemSlot() {
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.inventory.getItem(i >= 36 ? i - 36 : i);
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
}
