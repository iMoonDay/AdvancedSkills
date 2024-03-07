package com.imoonday.mixin;

import com.imoonday.init.ModEffectsKt;
import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Shadow
    private float stepHeight;

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$changeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setYaw", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$setYaw(float yaw, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setHeadYaw", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$setHeadYaw(float headYaw, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setBodyYaw", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$setBodyYaw(float bodyYaw, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setPitch", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$setPitch(float pitch, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "getStepHeight", at = @At("HEAD"), cancellable = true)
    private void advanced_skills$getStepHeight(CallbackInfoReturnable<Float> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            Float height = SkillTriggerHandler.INSTANCE.getStepHeight(player);
            if (height != null && height > this.stepHeight) {
                cir.setReturnValue(height);
            }
        }
    }

    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    private void advanced_skills$updateMovementInFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.INSTANCE.ignoreFluid(player, tag)) {
                cir.setReturnValue(false);
            }
        }
    }

    @ModifyVariable(method = "updateMovementInFluid", at = @At("HEAD"), argsOnly = true, index = 2)
    private double advanced_skills$modifySpeed(double value, TagKey<Fluid> tag, double speed) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            return SkillTriggerHandler.INSTANCE.getMovementInFluid(player, tag, value);
        }
        return speed;
    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void advanced_skills$isInvisible(CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.INSTANCE.isInvisible(player)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void advanced_skills$isInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity entity) {
            if (!SkillTriggerHandler.INSTANCE.isInvisibleTo(entity, player)) {
                cir.setReturnValue(false);
            }
        }
    }
}
