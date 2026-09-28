package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.Random;

public class KeySounds extends Module {
    public static final KeySounds INSTANCE = new KeySounds();

    public final ModeSetting soundPack = new ModeSetting("Switch", "Тип переключателей", "Cherry MX Blue",
            "Cherry MX Blue", "Cherry MX Brown", "Cherry MX Black", "NovelKeys Cream", "Holy Panda", "eSports Click");
    public final NumberSetting volume = new NumberSetting("Громкость", "Громкость звуков клавиатуры и мыши", 0.35, 0.05, 1.5, 0.05);
    public final BooleanSetting mouseClicks = new BooleanSetting("Mouse", "Звуки кликов мыши", true);
    public final BooleanSetting keyPresses = new BooleanSetting("Keyboard", "Звуки клавиш клавиатуры", true);

    private final Random random = new Random();

    public KeySounds() {
        super("KeySounds", "Звуки механической клавиатуры и мыши в стиле Mechvibes", Category.RENDER);
        addSettings(soundPack, volume, mouseClicks, keyPresses);
        setEnabled(false);
    }

    public void onKey(int key, int action) {
        if (!isEnabled() || !keyPresses.getValue() || action != 1 || mc.getSoundManager() == null) return;
        playSound(false);
    }

    public void onMouse(int button, int action) {
        if (!isEnabled() || !mouseClicks.getValue() || action != 1 || mc.getSoundManager() == null) return;
        playSound(true);
    }

    private void playSound(boolean isMouse) {
        float vol = volume.getValue().floatValue();
        float pitch = 0.92f + random.nextFloat() * 0.18f;

        if (isMouse) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch * 1.12f, vol * 0.85f));
            return;
        }

        switch (soundPack.getValue()) {
            case "Cherry MX Blue":
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch * 1.35f, vol));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DISPENSER_FAIL, pitch * 1.8f, vol * 0.35f));
                break;
            case "Cherry MX Brown":
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, pitch * 1.1f, vol));
                break;
            case "Cherry MX Black":
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, pitch * 0.95f, vol * 1.1f));
                break;
            case "NovelKeys Cream":
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASEDRUM, pitch * 1.4f, vol * 0.45f));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, pitch * 0.88f, vol));
                break;
            case "Holy Panda":
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, pitch * 0.92f, vol));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.STONE_BUTTON_CLICK_ON, pitch * 1.25f, vol * 0.5f));
                break;
            case "eSports Click":
            default:
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch, vol));
                break;
        }
    }
}
