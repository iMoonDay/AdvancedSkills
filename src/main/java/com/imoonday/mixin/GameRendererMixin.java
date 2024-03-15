package com.imoonday.mixin;

import com.imoonday.init.ModSkills;
import com.imoonday.trigger.SkillTriggerHandler;
import com.imoonday.util.PlayerUtilsKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void advanced_skills$getNightVisionStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof PlayerEntity player && PlayerUtilsKt.isUsing(player, ModSkills.NIGHT_VISION)) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V", shift = At.Shift.AFTER))
    private void advanced_skills$renderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        SkillTriggerHandler.INSTANCE.worldRender(matrices, tickDelta, this.client);
    }
}
