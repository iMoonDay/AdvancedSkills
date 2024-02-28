package com.imoonday.mixin;

import com.imoonday.init.ModEffectsKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I", shift = At.Shift.AFTER))
    public void advanced_skills$render(DrawContext context, float tickDelta, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player != null && player.getFrozenTicks() <= 0 && ModEffectsKt.isForceFrozen(player)) {
            this.renderOverlay(context, POWDER_SNOW_OUTLINE, 1.0f);
        }
    }
}
