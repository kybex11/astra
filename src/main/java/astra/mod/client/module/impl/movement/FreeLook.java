package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import org.lwjgl.glfw.GLFW;

public class FreeLook extends Module {
    public static FreeLook INSTANCE;

    public final ModeSetting mode = new ModeSetting("Mode", "Режим активации", "Hold", "Hold", "Toggle");
    public final NumberSetting sensitivity = new NumberSetting("Sens", "Чувствительность", 1.0, 0.2, 3.0, 0.1);
    public final BooleanSetting invertY = new BooleanSetting("InvertY", "Инверсия по вертикали", false);

    private static boolean active = false;
    public static float cameraYaw = 0.0f;
    public static float cameraPitch = 0.0f;
    private static CameraType originalCameraType = CameraType.FIRST_PERSON;

    public FreeLook() {
        super("FreeLook", "Свободный обзор на 360 градусов (на клавишу ALT)", Category.MOVEMENT, GLFW.GLFW_KEY_LEFT_ALT);
        INSTANCE = this;
        addSettings(mode, sensitivity, invertY);
    }

    public static boolean isActive() {
        return active;
    }

    public static void startFreeLook() {
        if (!active) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null && mc.options != null) {
                active = true;
                originalCameraType = mc.options.getCameraType();
                cameraYaw = mc.player.yRot;
                cameraPitch = mc.player.xRot;
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        }
    }

    public static void stopFreeLook() {
        if (active) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            active = false;
            if (mc.options != null) {
                mc.options.setCameraType(originalCameraType);
            }
        }
    }

    public static void onCameraTurn(double yRot, double xRot) {
        if (!active || INSTANCE == null) return;

        float sens = INSTANCE.sensitivity.getValue().floatValue();
        cameraYaw += (float) (yRot * 0.15D * sens);
        double pitchDelta = xRot * 0.15D * sens;
        if (INSTANCE.invertY.getValue()) {
            cameraPitch -= (float) pitchDelta;
        } else {
            cameraPitch += (float) pitchDelta;
        }
        cameraPitch = Math.max(-90.0F, Math.min(90.0F, cameraPitch));
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen != null) {
            if (active) stopFreeLook();
            return;
        }

        long handle = mc.getWindow().getWindow();
        boolean altDown = InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_ALT) ||
                          InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_RIGHT_ALT);

        if (mode.is("Hold")) {
            if (altDown) {
                if (!active) startFreeLook();
            } else {
                if (active) stopFreeLook();
            }
        }
    }

    @Override
    public void onEnable() {
        if (mode.is("Toggle")) {
            startFreeLook();
        }
    }

    @Override
    public void onDisable() {
        stopFreeLook();
    }
}
