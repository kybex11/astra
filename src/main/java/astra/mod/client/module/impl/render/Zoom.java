package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {
    public static final Zoom INSTANCE = new Zoom();

    public final NumberSetting zoomFactor = new NumberSetting("Множитель зума", 4.0, 1.5, 12.0, 0.5);
    public final NumberSetting smoothSpeed = new NumberSetting("Плавность анимации", 14.0, 4.0, 30.0, 1.0);
    public final BooleanSetting smoothMouse = new BooleanSetting("Плавная мышь", true);
    public final BooleanSetting mouseDamping = new BooleanSetting("Снижать чувств. мыши", true);
    public final BooleanSetting scrollZoom = new BooleanSetting("Зум колесиком", true);

    private float currentZoomProgress = 0.0f; // 0.0 = normal, 1.0 = full zoom
    private long lastFrameTime = System.currentTimeMillis();

    public Zoom() {
        super("Zoom", "Плавное приближение камеры по нажатию клавиши C", Category.RENDER);
        setKeyCode(GLFW.GLFW_KEY_C);
        setEnabled(true); // Active by default so holding C zooms immediately

        addSettings(zoomFactor, smoothSpeed, smoothMouse, mouseDamping, scrollZoom);
    }

    public boolean isHoldingZoom() {
        if (!isEnabled()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || mc.getWindow() == null) return false;

        int code = getKeyCode();
        if (code <= 0) return false;

        long handle = mc.getWindow().getWindow();
        if (code < -90) {
            int mouseBtn = -100 - code;
            return GLFW.glfwGetMouseButton(handle, mouseBtn) == GLFW.GLFW_PRESS;
        }

        return GLFW.glfwGetKey(handle, code) == GLFW.GLFW_PRESS;
    }

    public boolean isZooming() {
        return currentZoomProgress > 0.005f || isHoldingZoom();
    }

    public float getZoomProgress() {
        return currentZoomProgress;
    }

    public double modifyFov(double originalFov, float partialTicks) {
        long now = System.currentTimeMillis();
        float deltaSeconds = Math.min(0.1f, Math.max(0.001f, (now - lastFrameTime) / 1000.0f));
        lastFrameTime = now;

        float target = isHoldingZoom() ? 1.0f : 0.0f;
        float speed = ((Double) smoothSpeed.getValue()).floatValue();

        // Smooth cubic-like exponential animation
        float step = deltaSeconds * speed;
        currentZoomProgress = Mth.lerp(Math.min(1.0f, step), currentZoomProgress, target);

        if (currentZoomProgress < 0.001f) {
            currentZoomProgress = 0.0f;
            return originalFov;
        }

        // Apply smooth ease curve: s(t) = t * t * (3 - 2 * t)
        float smoothT = currentZoomProgress * currentZoomProgress * (3.0f - 2.0f * currentZoomProgress);
        double targetDiv = Math.max(1.1, (Double) zoomFactor.getValue());
        double fovMultiplier = 1.0 - (1.0 - (1.0 / targetDiv)) * smoothT;

        return Math.max(5.0, originalFov * fovMultiplier);
    }

    public boolean onScroll(double yoffset) {
        if (!scrollZoom.getValue() || !isHoldingZoom()) return false;

        double current = (Double) zoomFactor.getValue();
        double updated = Mth.clamp(current + (yoffset > 0 ? 0.5 : -0.5), 1.5, 12.0);
        zoomFactor.setValue(Math.round(updated * 10.0) / 10.0);
        return true;
    }

    public double getMouseSensitivityFactor() {
        if (!mouseDamping.getValue() || currentZoomProgress <= 0.01f) return 1.0;
        double targetDiv = Math.max(1.1, (Double) zoomFactor.getValue());
        return 1.0 / (1.0 + (targetDiv - 1.0) * currentZoomProgress);
    }
}
