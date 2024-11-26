package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.init.ModEffectsKt;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowAttackGoal.class)
public abstract class CrossbowAttackGoalMixin<T extends HostileEntity & CrossbowUser> {

    @Shadow
    @Final
    private T actor;

    @Shadow
    public abstract void stop();

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    public void advskills_re$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.actor)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldContinue", at = @At("RETURN"), cancellable = true)
    public void advskills_re$shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isDisarmed(this.actor)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advskills_re$tick(CallbackInfo ci) {
        if (ModEffectsKt.isSilenced(this.actor)) {
            this.stop();
            ci.cancel();
        }
    }
}
