package astra.mod.client.ui.hud;

import java.util.HashMap;
import java.util.Map;

public class HudConfig {
    public static final HudConfig INSTANCE = new HudConfig();

    public int fpsX = 8, fpsY = 8;
    public int cpsX = 70, cpsY = 8;
    public int comboX = 135, comboY = 8;
    public int keystrokesX = 20, keystrokesY = 60;
    public int armorX = -1, armorY = -1; // -1 means auto (bottom center)
    public int targetX = -1, targetY = -1; // -1 means auto (screen center bottom)
    public int miniMapX = 10, miniMapY = 140;

    public final Map<String, Float> elementScales = new HashMap<>();

    public float getScale(String id) {
        return elementScales.getOrDefault(id, 1.0f);
    }

    public void setScale(String id, float scale) {
        elementScales.put(id, Math.max(0.5f, Math.min(2.5f, Math.round(scale * 10.0f) / 10.0f)));
    }
}
