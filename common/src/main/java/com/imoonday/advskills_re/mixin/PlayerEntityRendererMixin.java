package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.trigger.DisguiseTrigger;
import com.imoonday.advskills_re.trigger.SkillTriggerHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(AbstractClientPlayerEntity player, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        DisguiseTrigger.DisguiseRenderer renderer = SkillTriggerHandler.INSTANCE.getDisguisingTarget(player);
        boolean rendered = false;
        if (renderer != null) {
            rendered = renderer.render(matrixStack, vertexConsumerProvider, i, g);
        }
        if (rendered || SkillTriggerHandler.INSTANCE.isInvisible(player)) {
            ci.cancel();
        } else {
            ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
            if (clientPlayer != null && clientPlayer != player && SkillTriggerHandler.INSTANCE.isInvisibleTo(player, clientPlayer)) {
                ci.cancel();
            }
        }
    }
}
