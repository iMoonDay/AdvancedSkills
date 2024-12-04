package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.client.modifier.SkillModifierHandler;
import com.imoonday.advskills_re.trigger.SkillTriggerHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void advskills_re$getNightVisionStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.hasNightVision(player)) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V", shift = At.Shift.AFTER))
    private void advskills_re$renderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        SkillModifierHandler.applyGameRendererModifiers(tickDelta, limitTime, matrices);
    }
}
