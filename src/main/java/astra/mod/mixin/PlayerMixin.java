package astra.mod.mixin;

import astra.mod.client.cosmetics.CosmeticManager;
import astra.mod.client.module.impl.render.ComboCounter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void onGetName(CallbackInfoReturnable<Component> cir) {
        if (CosmeticManager.INSTANCE.hasCustomName() && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(new TextComponent(CosmeticManager.INSTANCE.getCustomName()));
        }
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Component> cir) {
        if (CosmeticManager.INSTANCE.hasCustomName() && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(new TextComponent(CosmeticManager.INSTANCE.getCustomName()));
        }
    }

    @Inject(method = "isModelPartShown", at = @At("HEAD"), cancellable = true)
    private void onIsModelPartShown(PlayerModelPart part, CallbackInfoReturnable<Boolean> cir) {
        if (part == PlayerModelPart.CAPE && CosmeticManager.INSTANCE.isCapeEnabled()
                && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player) {
            ComboCounter.INSTANCE.onHit();
            try {
                if (astra.mod.client.module.impl.render.ReachDisplay.INSTANCE != null
                        && Minecraft.getInstance().player != null) {
                    double dist = Minecraft.getInstance().player.distanceTo(target);
                    astra.mod.client.module.impl.render.ReachDisplay.INSTANCE.record(dist);
                }
            } catch (Exception ignored) {}
        }
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player) {
            ComboCounter.INSTANCE.onTakeDamage();

            if (astra.mod.client.module.impl.player.AntiFire.INSTANCE != null && astra.mod.client.module.impl.player.AntiFire.INSTANCE.isEnabled()) {
                if (source.isFire() || source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE || source == DamageSource.LAVA) {
                    ci.cancel();
                    return;
                }
            }

            if (astra.mod.client.module.impl.player.GodMode.INSTANCE != null && astra.mod.client.module.impl.player.GodMode.INSTANCE.isEnabled()) {
                if (source == DamageSource.FALL || source.isFire() || source == DamageSource.LAVA || source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) {
                    ci.cancel();
                }
            }
        }
    }
}
