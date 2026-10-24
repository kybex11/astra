package astra.mod.mixin;

import astra.mod.client.module.impl.movement.FreeLook;
import astra.mod.client.module.impl.movement.Snaplook;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private Entity entity;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onCameraTick(CallbackInfo ci) {
        if (astra.mod.client.module.impl.movement.InstantSneak.INSTANCE != null
                && astra.mod.client.module.impl.movement.InstantSneak.INSTANCE.isEnabled()
                && this.entity != null) {
            this.eyeHeight = this.entity.getEyeHeight();
            this.eyeHeightOld = this.eyeHeight;
        }
    }

    @Inject(method = "setup", at = @At("HEAD"))
    private void onSetupHead(BlockGetter blockGetter, Entity entity, boolean thirdPerson, boolean mirrored, float partialTicks, CallbackInfo ci) {
        if (astra.mod.client.module.impl.movement.InstantSneak.INSTANCE != null
                && astra.mod.client.module.impl.movement.InstantSneak.INSTANCE.isEnabled()
                && entity != null) {
            this.eyeHeight = entity.getEyeHeight();
            this.eyeHeightOld = this.eyeHeight;
        }
    }

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", shift = At.Shift.AFTER))
    private void onSetupRotation(BlockGetter blockGetter, Entity entity, boolean thirdPerson, boolean mirrored, float partialTicks, CallbackInfo ci) {
        if (FreeLook.isActive()) {
            this.setRotation(FreeLook.cameraYaw, FreeLook.cameraPitch);
        }
        if (Snaplook.isFlipped()) {
            this.setRotation(Snaplook.snapYaw, Snaplook.snapPitch);
        }
    }
}
