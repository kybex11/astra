package astra.mod.client.module.impl.player;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import net.minecraft.core.BlockPos;

public class SafeWalk extends Module {
    public static final SafeWalk INSTANCE = new SafeWalk();

    public final ModeSetting mode = new ModeSetting("Режим", "Тип шифта на краю", "Eagle", "Eagle", "Legit", "Continuous");
    public final BooleanSetting blocksOnly = new BooleanSetting("HoldingBlocks", "Срабатывать только когда в руке блоки", true);

    public SafeWalk() {
        super("SafeWalk", "Автоматический шифт на самом краю блока (Eagle / FastBridge)", Category.BUILD);
        addSettings(mode, blocksOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (blocksOnly.getValue()) {
            boolean hasBlock = mc.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem
                    || mc.player.getOffhandItem().getItem() instanceof net.minecraft.world.item.BlockItem;
            if (!hasBlock) return;
        }

        if (mc.player.isOnGround()) {
            BlockPos under = mc.player.blockPosition().below();
            if (mc.level.getBlockState(under).isAir()) {
                mc.options.keyShift.setDown(true);
            }
        }
    }
}
