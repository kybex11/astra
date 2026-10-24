package astra.mod.mixin;

import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onChatInit(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            mc.options.chatVisibility = ChatVisiblity.FULL;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onChatRender(PoseStack poseStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int accent = ThemeManager.INSTANCE.getAccent();
        int boxX = 2;
        int boxY = this.height - 16;
        int boxW = this.width - 4;
        int boxH = 14;

        // Modern glass chat input background
        RenderUtils.drawRoundedBorderedRect(poseStack, boxX, boxY, boxW, boxH, 3, 1, 0xDD0B0F19, accent);
        // Prefix badge
        if (this.minecraft != null && this.minecraft.font != null) {
            this.minecraft.font.drawShadow(poseStack, "💬 >", boxX + 4, boxY + 3, accent);
        }
    }
}
