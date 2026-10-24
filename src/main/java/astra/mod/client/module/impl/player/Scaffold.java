package astra.mod.client.module.impl.player;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Scaffold extends Module {
    public static final Scaffold INSTANCE = new Scaffold();

    public final ModeSetting mode = new ModeSetting("Режим", "Стиль наведения и строительства", "GodBridge", "Normal", "GodBridge", "Moonwalk", "TellyBridge", "Expand");
    public final BooleanSetting tower = new BooleanSetting("Tower", "Быстрый подъем вверх при прыжке", true);
    public final BooleanSetting safeWalk = new BooleanSetting("SafeWalk", "Защита от падения с края блока", true);
    public final BooleanSetting eagle = new BooleanSetting("Eagle (AutoSneak)", "Автоматический шифт на самом краю блока", true);
    public final BooleanSetting silentRotations = new BooleanSetting("Rotations", "Легитная фиксация взгляда на блоке", true);
    public final NumberSetting expand = new NumberSetting("Expand", "Дальность выноса блоков вперед", 1.0, 1.0, 4.0, 1.0);
    public final NumberSetting delay = new NumberSetting("Задержка", "Задержка между установкой блоков (мс)", 10.0, 0.0, 200.0, 5.0);

    private long lastPlaceTime = 0;

    public Scaffold() {
        super("Scaffold", "Режим строительства: автоматическая кладка блоков, GodBridge, Moonwalk и Telly", Category.BUILD);
        addSettings(mode, tower, safeWalk, eagle, silentRotations, expand, delay);
    }

    private static class BlockData {
        public final BlockPos pos;
        public final Direction face;

        public BlockData(BlockPos pos, Direction face) {
            this.pos = pos;
            this.face = face;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        long now = System.currentTimeMillis();
        if (now - lastPlaceTime < delay.getValue()) return;

        BlockPos playerPos = mc.player.blockPosition();
        BlockPos underPlayer = playerPos.below();

        int expandDist = (int) Math.round(expand.getValue());
        Direction forwardDir = mc.player.getDirection();

        BlockPos targetAirPos = underPlayer;
        if (mode.is("Expand") && expandDist > 1) {
            targetAirPos = targetAirPos.relative(forwardDir, expandDist - 1);
        }

        // Only place if target is air or replaceable
        if (isReplaceable(targetAirPos)) {
            BlockData data = getBlockData(targetAirPos);
            if (data != null) {
                int blockSlot = findBlockSlot();
                if (blockSlot != -1) {
                    int oldSlot = mc.player.inventory.selected;
                    mc.player.inventory.selected = blockSlot;

                    if (silentRotations.getValue()) {
                        mc.player.xRot = 78.5f;
                    }

                    // Calculate precise hit vector on solid neighbor face
                    Vec3 hitVec = new Vec3(
                            data.pos.getX() + 0.5 + data.face.getStepX() * 0.5,
                            data.pos.getY() + 0.5 + data.face.getStepY() * 0.5,
                            data.pos.getZ() + 0.5 + data.face.getStepZ() * 0.5
                    );

                    BlockHitResult hitResult = new BlockHitResult(hitVec, data.face, data.pos, false);

                    if (mc.gameMode != null) {
                        mc.gameMode.useItemOn(mc.player, mc.level, InteractionHand.MAIN_HAND, hitResult);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }

                    if (tower.getValue() && mc.options.keyJump.isDown()) {
                        mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.42, mc.player.getDeltaMovement().z);
                    }

                    lastPlaceTime = now;
                }
            }
        }

        if (eagle.getValue() && mc.player.isOnGround()) {
            BlockPos currentUnder = mc.player.blockPosition().below();
            if (isReplaceable(currentUnder)) {
                mc.options.keyShift.setDown(true);
            }
        }
    }

    private BlockData getBlockData(BlockPos target) {
        // Search adjacent solid neighbors to click against
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.relative(dir);
            BlockState state = mc.level.getBlockState(neighbor);
            if (!state.isAir() && state.getMaterial().isSolid()) {
                return new BlockData(neighbor, dir.getOpposite());
            }
        }
        // Check secondary diagonal neighbors
        for (Direction dir : Direction.values()) {
            BlockPos step = target.relative(dir);
            for (Direction dir2 : Direction.values()) {
                BlockPos neighbor = step.relative(dir2);
                BlockState state = mc.level.getBlockState(neighbor);
                if (!state.isAir() && state.getMaterial().isSolid()) {
                    return new BlockData(neighbor, dir2.getOpposite());
                }
            }
        }
        return null;
    }

    private boolean isReplaceable(BlockPos pos) {
        if (mc.level == null) return false;
        BlockState state = mc.level.getBlockState(pos);
        return state.isAir() || state.getMaterial().isReplaceable();
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSafeWalk() {
        return isEnabled() && safeWalk.getValue();
    }
}
