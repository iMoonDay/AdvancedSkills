package com.imoonday.advanced_skills_re.forge.mixin;

import com.imoonday.init.ModEffectsKt;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowAttackGoal.class)
public abstract class BowAttackGoalMixin<T extends MobEntity> {

    @Shadow
    @Final
    private T actor;

    @Shadow
    public abstract void stop();

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    public void advanced_skills_re$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.actor)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldContinue", at = @At("RETURN"), cancellable = true)
    public void advanced_skills_re$shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.actor)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$tick(CallbackInfo ci) {
        if (ModEffectsKt.isDisarmed(this.actor)) {
            this.stop();
            ci.cancel();
        }
    }
}
