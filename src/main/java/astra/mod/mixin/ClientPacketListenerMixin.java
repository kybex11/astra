package astra.mod.mixin;

import astra.mod.client.module.impl.combat.Velocity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void onHandleSetEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && packet.getId() == mc.player.getId()) {
            if (Velocity.INSTANCE != null && Velocity.INSTANCE.isEnabled()) {
                double h = Velocity.INSTANCE.horizontal.getValue() / 100.0;
                double v = Velocity.INSTANCE.vertical.getValue() / 100.0;
                if (h == 0.0 && v == 0.0) {
                    ci.cancel();
                } else {
                    mc.player.setDeltaMovement(
                            (packet.getXa() / 8000.0) * h,
                            (packet.getYa() / 8000.0) * v,
                            (packet.getZa() / 8000.0) * h
                    );
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"), cancellable = true)
    private void onHandleSetTime(net.minecraft.network.protocol.game.ClientboundSetTimePacket packet, CallbackInfo ci) {
        if (astra.mod.client.module.impl.render.TimeChanger.INSTANCE != null && astra.mod.client.module.impl.render.TimeChanger.INSTANCE.isEnabled()) {
            ci.cancel();
        }
    }
}
