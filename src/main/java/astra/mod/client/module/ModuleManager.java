package astra.mod.client.module;

import astra.mod.client.Category;
import astra.mod.client.module.impl.combat.AimAssist;
import astra.mod.client.module.impl.combat.AutoClicker;
import astra.mod.client.module.impl.combat.EntityLauncher;
import astra.mod.client.module.impl.combat.KillAura;
import astra.mod.client.module.impl.combat.Reach;
import astra.mod.client.module.impl.combat.Velocity;
import astra.mod.client.module.impl.movement.FastClimb;
import astra.mod.client.module.impl.movement.Fly;
import astra.mod.client.module.impl.movement.FreeLook;
import astra.mod.client.module.impl.movement.Speed;
import astra.mod.client.module.impl.movement.Sprint;
import astra.mod.client.module.impl.player.AutoTotem;
import astra.mod.client.module.impl.player.FastPlace;
import astra.mod.client.module.impl.player.GodMode;
import astra.mod.client.module.impl.player.NoFall;
import astra.mod.client.module.impl.player.NoHunger;
import astra.mod.client.module.impl.player.SafeWalk;
import astra.mod.client.module.impl.player.Scaffold;
import astra.mod.client.module.impl.render.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

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
        register(AimAssist.INSTANCE);
        register(new AutoClicker());
        register(new EntityLauncher());
        register(new Velocity());
        register(new Reach());

        // Movement
        register(new Sprint());
        register(FastClimb.INSTANCE);
        register(astra.mod.client.module.impl.movement.AntiSwimming.INSTANCE);
        register(new Fly());
        register(new Speed());
        register(new FreeLook());

        // Render & Visuals
        register(new Fullbright());
        register(new ESP());
        register(new HUD());
        register(ComboCounter.INSTANCE);
        register(astra.mod.client.module.impl.render.KeySounds.INSTANCE);
        register(new Keystrokes());
        register(new ArmorHUD());
        register(new TargetHUD());
        register(new CPSMod());
        register(new FPSMod());
        register(new CustomCrosshair());
        register(new NoHurtCam());
        register(new WeatherChanger());
        register(new TimeChanger());
        register(new ClickGuiModule());
        register(new DarkTheme());
        register(new MiniMap());
        register(new FastRender());
        register(new NoRender());
        register(new Trails());

        // Player & Building
        register(new NoFall());
        register(new FastPlace());
        register(SafeWalk.INSTANCE);
        register(Scaffold.INSTANCE);
        register(NoHunger.INSTANCE);
        register(astra.mod.client.module.impl.player.AntiFire.INSTANCE);
        register(new GodMode());
        register(new AutoTotem());
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

    public void handleKey(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return;
        for (Module module : modules) {
            if (module.getKeyCode() == keyCode) {
                module.toggle();
            }
        }
    }

    public void handleMouse(int button) {
        int mappedCode = -100 - button;
        for (Module module : modules) {
            if (module.getKeyCode() == mappedCode) {
                module.toggle();
            }
        }
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
