package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class Coordinates extends Module {
    public static Coordinates INSTANCE;

    public final BooleanSetting showBiome = new BooleanSetting("Биом", "Показывать название биома", true);
    public final BooleanSetting showFacing = new BooleanSetting("Сторона", "Показывать сторону света", true);

    public Coordinates() {
        super("Coordinates", "Координаты XYZ, биом и сторона света", Category.HUD);
        INSTANCE = this;
        addSettings(showBiome, showFacing);
        setEnabled(true);
    }

    public static String getBiomeName() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return "?";
            net.minecraft.core.BlockPos pos = mc.player.blockPosition();
            net.minecraft.world.level.biome.Biome biome = mc.level.getBiome(pos);
            if (biome == null) return "?";
            String name = biome.getBiomeCategory().name();
            if (name.isEmpty()) return "?";
            return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        } catch (Exception e) {
            return "?";
        }
    }

    public static String getFacing() {
        try {
            float yaw = ((net.minecraft.client.Minecraft.getInstance().player.yRot % 360.0f) + 360.0f) % 360.0f;
            if (yaw < 22.5f || yaw >= 337.5f) return "South (+Z)";
            if (yaw < 67.5f) return "South-West";
            if (yaw < 112.5f) return "West (-X)";
            if (yaw < 157.5f) return "North-West";
            if (yaw < 202.5f) return "North (-Z)";
            if (yaw < 247.5f) return "North-East";
            if (yaw < 292.5f) return "East (+X)";
            return "South-East";
        } catch (Exception e) {
            return "?";
        }
    }
}
