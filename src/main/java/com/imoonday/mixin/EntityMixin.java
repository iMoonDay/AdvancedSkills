package com.imoonday.mixin;

import com.imoonday.init.ModEffectsKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

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
}
