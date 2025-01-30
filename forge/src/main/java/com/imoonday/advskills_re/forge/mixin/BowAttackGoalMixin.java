package com.imoonday.advskills_re.forge.mixin;

import com.imoonday.advskills_re.init.ModEffectsKt;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowAttackGoal.class)
public abstract class BowAttackGoalMixin {

    @Unique
    private MobEntity advskills_re$actor;

    @Shadow
    public abstract void stop();

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/MobEntity;DIF)V", at = @At("RETURN"))
    public void advskills_re$init(MobEntity arg, double d, int i, float f, CallbackInfo ci) {
        this.advskills_re$actor = arg;
    }

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    public void advskills_re$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.advskills_re$actor)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldContinue", at = @At("RETURN"), cancellable = true)
    public void advskills_re$shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.advskills_re$actor)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advskills_re$tick(CallbackInfo ci) {
        if (ModEffectsKt.isDisarmed(this.advskills_re$actor)) {
            this.stop();
            ci.cancel();
        }
    }
}
