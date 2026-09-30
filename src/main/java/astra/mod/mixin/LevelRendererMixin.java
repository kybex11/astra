package astra.mod.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    // 3D rendering is handled by Fabric's WorldRenderEvents.LAST in AstraClient for exact matrix synchronization
}
