package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.mixin.MinecraftAccessor;
import net.minecraft.world.InteractionHand;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AutoClicker extends Module {
    public static AutoClicker INSTANCE;

    public final NumberSetting cps = new NumberSetting("CPS", "Кликов в секунду", 14.0, 1.0, 20.0, 1.0);
    public final BooleanSetting jitter = new BooleanSetting("Jitter", "Человеческая рандомизация задержек", true);

    private long nextClickTime = 0L;
    private final Random random = new Random();

    public AutoClicker() {
        super("AutoClicker", "Автоматически кликает при зажатии левой кнопки мыши", Category.COMBAT);
        INSTANCE = this;
        addSettings(cps, jitter);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen != null) return;

        long window = mc.getWindow().getWindow();
        boolean lmbDown = mc.options.keyAttack.isDown() ||
                          (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS);

        if (lmbDown) {
            long now = System.currentTimeMillis();
            if (now >= nextClickTime) {
                if (mc instanceof MinecraftAccessor) {
                    ((MinecraftAccessor) mc).setMissTime(0);
                    ((MinecraftAccessor) mc).callStartAttack();
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }

                double targetCps = cps.getValue();
                long baseDelay = (long) (1000.0 / targetCps);
                if (jitter.getValue()) {
                    long randomOffset = (long) ((random.nextDouble() - 0.5) * (baseDelay * 0.3));
                    baseDelay += randomOffset;
                }
                nextClickTime = now + Math.max(25L, baseDelay);
            }
        }
    }
}
