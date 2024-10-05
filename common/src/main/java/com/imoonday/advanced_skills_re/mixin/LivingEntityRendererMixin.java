package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "shouldFlipUpsideDown", at = @At("HEAD"), cancellable = true)
    private static void advanced_skills$shouldFlipUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.shouldFlipUpsideDown(player)) {
            cir.setReturnValue(true);
        }
    }
}
