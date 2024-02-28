package com.imoonday.mixin;

import com.imoonday.components.UsingSkillComponentKt;
import com.imoonday.skills.Skills;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void advanced_skills$getNightVisionStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof PlayerEntity player && UsingSkillComponentKt.isUsingSkill(player, Skills.NIGHT_VISION)) {
            cir.setReturnValue(1.0f);
        }
    }
}
