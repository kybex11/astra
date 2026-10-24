package astra.mod.mixin;

import astra.mod.client.module.impl.render.NametagMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "renderNameTag", at = @At("HEAD"))
    private void onRenderNameTagHead(Entity entity, Component name, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        try {
            NametagMod m = NametagMod.INSTANCE;
            if (m == null || !m.isEnabled()) return;
            if (!(entity instanceof Player)) return;
            if (entity == Minecraft.getInstance().player && !m.showOwn.getValue()) return;
            float s = m.scale.getValue().floatValue();
            if (s != 1.0f) poseStack.scale(s, s, s);
        } catch (Exception ignored) {}
    }

    @Inject(method = "shouldShowName", at = @At("RETURN"), cancellable = true)
    private void onShouldShowName(Entity entity, CallbackInfoReturnable<Boolean> cir) {        try {
            if (cir.getReturnValue()) return;
            NametagMod m = NametagMod.INSTANCE;
            if (m == null || !m.isEnabled()) return;
            if (!(entity instanceof Player)) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || entity == mc.player) return;
            double maxDist = m.maxDistance.getValue();
            if (maxDist > 0.0 && entity.distanceToSqr(mc.player) <= maxDist * maxDist) {
                cir.setReturnValue(true);
            }
        } catch (Exception ignored) {}
    }

    // Ванилла режет ники дальше 64 блоков внутри renderNameTag — подменяем замер,
    // чтобы настройка дистанции реально работала.
    @Redirect(method = "renderNameTag",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    private double redirectNametagDistance(net.minecraft.client.renderer.entity.EntityRenderDispatcher dispatcher, Entity entity) {
        try {
            NametagMod m = NametagMod.INSTANCE;
            Minecraft mc = Minecraft.getInstance();
            if (m != null && m.isEnabled() && entity instanceof Player
                    && mc.player != null && entity != mc.player
                    && m.maxDistance.getValue() > 0.0
                    && entity.distanceToSqr(mc.player) <= m.maxDistance.getValue() * m.maxDistance.getValue()) {
                return 0.0;
            }
        } catch (Exception ignored) {}
        return dispatcher.distanceToSqr(entity);
    }
}
