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

    // info pills
    public int coordsX = 8, coordsY = 26;
    public int pingX = 8, pingY = 40;
    public int directionX = 120, directionY = 26;
    public int momentumX = 120, momentumY = 40;
    public int saturationX = 95, saturationY = 54;
    public int totemX = 10, totemY = 225;
    public int itemCountX = 10, itemCountY = 239;
    public int reachX = 10, reachY = 253;
    public int stopwatchX = 10, stopwatchY = 267;
    public int effectsX = -1, effectsY = -1; // -1 means auto (top-right)
    public int bossBarX = -1, bossBarY = 12; // -1 means auto (top-center)

    public final Map<String, Float> elementScales = new HashMap<>();

    public float getScale(String id) {
        return elementScales.getOrDefault(id, 1.0f);
    }

    public void setScale(String id, float scale) {
        elementScales.put(id, Math.max(0.5f, Math.min(2.5f, Math.round(scale * 10.0f) / 10.0f)));
    }
}
