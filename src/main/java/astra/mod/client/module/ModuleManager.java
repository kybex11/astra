package astra.mod.client.module;

import astra.mod.client.Category;
import astra.mod.client.module.impl.combat.AutoClicker;
import astra.mod.client.module.impl.combat.KillAura;
import astra.mod.client.module.impl.movement.Fly;
import astra.mod.client.module.impl.movement.FreeLook;
import astra.mod.client.module.impl.movement.Speed;
import astra.mod.client.module.impl.movement.Sprint;
import astra.mod.client.module.impl.player.FastPlace;
import astra.mod.client.module.impl.player.NoFall;
import astra.mod.client.module.impl.render.*;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Combat
        register(new KillAura());
        register(new AutoClicker());

        // Movement
        register(new Sprint());
        register(new Fly());
        register(new Speed());
        register(new FreeLook());

        // Render & Lunar Visuals
        register(new Fullbright());
        register(new ESP());
        register(new HUD());
        register(new Keystrokes());
        register(new ArmorHUD());
        register(new TargetHUD());
        register(new CPSMod());
        register(new FPSMod());
        register(new CustomCrosshair());
        register(new NoHurtCam());
        register(new PvPPackModule());
        register(new ClickGuiModule());

        // Player
        register(new NoFall());
        register(new FastPlace());
    }

    private void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (module.getClass() == clazz) {
                return (T) module;
            }
        }
        return null;
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        for (Module module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    System.err.println("Error ticking module " + module.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
