package astra.mod.client.module.impl.misc;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.StringSetting;

import java.util.Locale;

public class AutoGG extends Module {
    public static AutoGG INSTANCE;

    public final StringSetting message = new StringSetting("Сообщение", "Что отправлять после победы", "gg!");
    public final NumberSetting delay = new NumberSetting("Задержка", "Пауза перед отправкой (сек)", 2.0, 0.0, 10.0, 1.0);

    private static final String[] TRIGGERS = {
            " won the game", " victory", " победи", " выиграл", " выиграла",
            "- winner:", " game over", "1st place", "1-е место", " champion"
    };

    private long sendAtMs = 0L;

    public AutoGG() {
        super("AutoGG", "Автоматически пишет gg после победы", Category.MISC);
        INSTANCE = this;
        addSettings(message, delay);
        setEnabled(false);
    }

    public static void onChatMessage(String lowerText) {
        try {
            if (INSTANCE == null || !INSTANCE.isEnabled()) return;
            String t = lowerText.toLowerCase(Locale.ROOT);
            for (String trigger : TRIGGERS) {
                if (t.contains(trigger)) {
                    INSTANCE.sendAtMs = System.currentTimeMillis()
                            + (long) (INSTANCE.delay.getValue() * 1000.0);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onTick() {
        if (sendAtMs != 0L && System.currentTimeMillis() >= sendAtMs) {
            sendAtMs = 0L;
            if (mc.player != null && mc.screen == null) {
                String msg = message.getValue();
                if (msg != null && !msg.trim().isEmpty()) {
                    try {
                        mc.player.chat(msg.trim());
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Override
    public void onDisable() {
        sendAtMs = 0L;
    }
}
