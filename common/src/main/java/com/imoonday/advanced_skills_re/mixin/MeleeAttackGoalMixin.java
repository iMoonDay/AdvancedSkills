package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.init.ModEffectsKt;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {

    @Shadow
    @Final
    protected PathAwareEntity mob;

    @Shadow
    public abstract void stop();

    @Shadow
    protected abstract void resetCooldown();

    @ModifyReturnValue(method = "canStart", at = @At("RETURN"))
    public boolean advanced_skills$canStart(boolean original) {
        return !ModEffectsKt.isDisarmed(this.mob) && original;
    }

    @ModifyReturnValue(method = "shouldContinue", at = @At("RETURN"))
    public boolean advanced_skills$shouldContinue(boolean original) {
        return !ModEffectsKt.isDisarmed(this.mob) && original;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$tick(CallbackInfo ci) {
        if (ModEffectsKt.isDisarmed(this.mob)) {
            this.stop();
            ci.cancel();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$attack(LivingEntity target, double squaredDistance, CallbackInfo ci) {
        if (ModEffectsKt.isDisarmed(this.mob)) {
            this.resetCooldown();
            ci.cancel();
        }
    }
}
