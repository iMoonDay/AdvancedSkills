package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.init.ModEffectsKt;
import net.minecraft.entity.mob.GhastEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.mob.GhastEntity$ShootFireballGoal")
public abstract class GhastEntity$ShootFireballGoalMixin {

    @Shadow
    @Final
    private GhastEntity ghast;

    @Shadow
    public abstract void stop();

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    public void advskills_re$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isSilenced(this.ghast)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advskills_re$tick(CallbackInfo ci) {
        if (ModEffectsKt.isSilenced(this.ghast)) {
            this.stop();
            ci.cancel();
        }
    }
}
