package astra.mod.mixin;

import astra.mod.client.module.impl.render.ItemPhysics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(ItemEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        try {
            if (ItemPhysics.INSTANCE != null && ItemPhysics.INSTANCE.isEnabled() && !entity.getItem().isEmpty()) {
                float speed = ItemPhysics.INSTANCE.spinSpeed.getValue().floatValue();
                if (speed > 0.0f) {
                    float angle = ((entity.getAge() + partialTicks) * speed * 3.0f) % 360.0f;
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(angle));
                }
            }
        } catch (Exception ignored) {}
    }
}
