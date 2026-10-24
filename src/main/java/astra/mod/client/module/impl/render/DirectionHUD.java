package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class DirectionHUD extends Module {
    public static DirectionHUD INSTANCE;

    public final BooleanSetting showDegrees = new BooleanSetting("Градусы", "Показывать градусы поворота", true);

    public DirectionHUD() {
        super("DirectionHUD", "Компас: направление взгляда", Category.HUD);
        INSTANCE = this;
        addSettings(showDegrees);
        setEnabled(true);
    }

    public static String getDirection() {
        try {
            float yaw = ((net.minecraft.client.Minecraft.getInstance().player.yRot % 360.0f) + 360.0f) % 360.0f;
            int deg = Math.round(yaw);
            String dir;
            if (yaw < 22.5f || yaw >= 337.5f) dir = "S";
            else if (yaw < 67.5f) dir = "SW";
            else if (yaw < 112.5f) dir = "W";
            else if (yaw < 157.5f) dir = "NW";
            else if (yaw < 202.5f) dir = "N";
            else if (yaw < 247.5f) dir = "NE";
            else if (yaw < 292.5f) dir = "E";
            else dir = "SE";
            return dir + " (" + deg + "°)";
        } catch (Exception e) {
            return "?";
        }
    }
}
