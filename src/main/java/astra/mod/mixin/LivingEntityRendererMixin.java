package astra.mod.mixin;

import astra.mod.client.module.impl.render.MobSize;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        try {
            if (MobSize.INSTANCE != null && MobSize.INSTANCE.isEnabled()
                    && entity instanceof Mob && !(entity instanceof Player)) {
                float s = MobSize.INSTANCE.scale.getValue().floatValue();
                if (s != 1.0f) poseStack.scale(s, s, s);
            }
        } catch (Exception ignored) {}
    }
}
