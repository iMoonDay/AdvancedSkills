package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.init.ModEffectsKt;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$TeleportTowardsPlayerGoal")
public abstract class EndermanEntity$TeleportTowardsPlayerGoalMixin {

    @Shadow
    @Final
    private EndermanEntity enderman;

    @Shadow
    public abstract void stop();

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    public void advanced_skills_re$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isSilenced(this.enderman)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$tick(CallbackInfo ci) {
        if (ModEffectsKt.isSilenced(this.enderman)) {
            this.stop();
            ci.cancel();
        }
    }
}
