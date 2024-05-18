package com.imoonday.mixin;

import com.imoonday.init.ModKeyBindings;
import com.imoonday.screen.SkillInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "keyPressed", at = @At("RETURN"), cancellable = true)
    public void advanced_skills$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if ((HandledScreen<?>) (Object) this instanceof AbstractInventoryScreen<?> screen) {
            if (ModKeyBindings.OPEN_LIST_SCREEN.matchesKey(keyCode, scanCode)) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.setScreen(new SkillInventoryScreen(client.player, () -> screen));
                }
                cir.setReturnValue(true);
            }
        }
    }
}
