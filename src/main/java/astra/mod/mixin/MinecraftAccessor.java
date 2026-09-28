package astra.mod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Invoker("startAttack")
    void callStartAttack();

    @Accessor("missTime")
    void setMissTime(int missTime);

    @Accessor("missTime")
    int getMissTime();

    @Accessor("user")
    @Mutable
    void setUser(User user);
}
