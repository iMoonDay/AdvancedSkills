package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.init.ModEffectsKt;
import com.imoonday.advskills_re.skill.trigger.SkillTriggerHandler;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "shouldFlipUpsideDown", at = @At("HEAD"), cancellable = true)
    private static void advskills_re$shouldFlipUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.shouldFlipUpsideDown(player)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;animateModel(Lnet/minecraft/entity/Entity;FFF)V"))
    private void advskills_re$animateModel(Args args) {
        LivingEntity entity = args.get(0);
        if (ModEffectsKt.isForceFrozen(entity) || ModEffectsKt.isConfined(entity)) {
            args.set(1, 0f);
            args.set(2, 0f);
        }
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V"))
    private void advskills_re$setAngles(Args args) {
        LivingEntity entity = args.get(0);
        if (ModEffectsKt.isForceFrozen(entity) || ModEffectsKt.isConfined(entity)) {
            args.set(1, 0f);
            args.set(2, 0f);
        }
    }
}
