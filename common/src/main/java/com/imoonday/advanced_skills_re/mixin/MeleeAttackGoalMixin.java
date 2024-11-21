package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.init.ModEffectsKt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {

    @Shadow
    @Final
    protected PathAwareEntity mob;

    @Shadow
    public abstract void stop();

    @Shadow
    protected abstract void resetCooldown();

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    public void advanced_skills_re$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.mob)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldContinue", at = @At("RETURN"), cancellable = true)
    public void advanced_skills_re$shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.mob)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$tick(CallbackInfo ci) {
        if (ModEffectsKt.isDisarmed(this.mob)) {
            this.stop();
            ci.cancel();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$attack(LivingEntity target, double squaredDistance, CallbackInfo ci) {
        if (ModEffectsKt.isDisarmed(this.mob)) {
            this.resetCooldown();
            ci.cancel();
        }
    }
}
