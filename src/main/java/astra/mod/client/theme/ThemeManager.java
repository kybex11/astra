package astra.mod.client.theme;

import java.awt.Color;

public class ThemeManager {
    public static final ThemeManager INSTANCE = new ThemeManager();

    public enum Theme {
        INDIGO("Indigo", 0xFF6366F1, 0xFF8B5CF6, 0xFF4338CA),
        CYAN("Neon Cyan", 0xFF06B6D4, 0xFF3B82F6, 0xFF0891B2),
        EMERALD("Emerald", 0xFF10B981, 0xFF059669, 0xFF047857),
        CRIMSON("Crimson", 0xFFEF4444, 0xFFDC2626, 0xFFB91C1C),
        SUNSET("Sunset", 0xFFF97316, 0xFFF59E0B, 0xFFD97706),
        SAKURA("Sakura", 0xFFEC4899, 0xFFA855F7, 0xFFDB2777),
        CHROMA("Rainbow", 0, 0, 0);

        private final String displayName;
        private final int primary;
        private final int secondary;
        private final int dark;

        Theme(String displayName, int primary, int secondary, int dark) {
            this.displayName = displayName;
            this.primary = primary;
            this.secondary = secondary;
            this.dark = dark;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getPrimary() {
            if (this == CHROMA) {
                return getRainbow(0);
            }
            return primary;
        }

        public int getSecondary() {
            if (this == CHROMA) {
                return getRainbow(500);
            }
            return secondary;
        }

        public int getDark() {
            if (this == CHROMA) {
                return (getRainbow(0) & 0x00FFFFFF) | 0x88000000;
            }
            return dark;
        }
    }

    private Theme currentTheme = Theme.INDIGO;
    private boolean particles = true;
    private float guiAlpha = 0.92f;

    public static int getRainbow(int offset) {
        float hue = ((System.currentTimeMillis() + offset) % 3000L) / 3000.0F;
        int rgb = Color.HSBtoRGB(hue, 0.75F, 1.0F);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        this.currentTheme = theme;
    }

    public void cycleTheme() {
        Theme[] values = Theme.values();
        int next = (currentTheme.ordinal() + 1) % values.length;
        this.currentTheme = values[next];
    }

    public int getAccent() {
        return currentTheme.getPrimary();
    }

    public int getAccentSecondary() {
        return currentTheme.getSecondary();
    }

    public int getAccentDark() {
        return currentTheme.getDark();
    }

    public boolean isParticles() {
        return particles;
    }

    public void setParticles(boolean particles) {
        this.particles = particles;
    }

    public float getGuiAlpha() {
        return guiAlpha;
    }

    public void setGuiAlpha(float guiAlpha) {
        this.guiAlpha = guiAlpha;
    }
}
