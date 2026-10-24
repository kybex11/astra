package astra.mod.mixin;

import astra.mod.client.cosmetics.CosmeticRenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRenderDispatcher dispatcher, PlayerModel<AbstractClientPlayer> model, float shadow) {
        super(dispatcher, model, shadow);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Z)V", at = @At("RETURN"))
    private void onPlayerRendererInit(EntityRenderDispatcher dispatcher, boolean slim, CallbackInfo ci) {
        this.addLayer(new CosmeticRenderLayer(this));
    }
}
