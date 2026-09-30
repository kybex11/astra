package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class OofMod extends Module {
    public static OofMod INSTANCE;

    private boolean wasDead = false;

    public OofMod() {
        super("OofMod", "Забавный звук при смерти игрока", Category.RENDER);
        INSTANCE = this;
        setEnabled(false);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getSoundManager() == null) {
            wasDead = false;
            return;
        }
        boolean dead = mc.player.isDeadOrDying() || mc.player.getHealth() <= 0.0f;
        if (dead && !wasDead) {
            try {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASEDRUM, 0.6f, 1.0f));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_DEATH, 1.2f, 0.5f));
            } catch (Exception ignored) {}
        }
        if (!dead) wasDead = false;
        else wasDead = true;
    }
}
