package astra.mod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "chat", at = @At("HEAD"), cancellable = true)
    private void onChat(String message, CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            mc.options.chatVisibility = ChatVisiblity.FULL;
        }
        if (player.connection != null) {
            player.connection.send(new ServerboundChatPacket(message));
            ci.cancel();
        }
    }
}
