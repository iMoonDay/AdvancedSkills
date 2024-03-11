package com.imoonday.mixin;

import com.imoonday.init.ModEffectsKt;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.mob.BlazeEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.entity.mob.BlazeEntity$ShootFireballGoal")
public abstract class BlazeEntity$ShootFireballGoalMixin {

    @Shadow
    @Final
    private BlazeEntity blaze;

    @Shadow
    public abstract void stop();

    @ModifyReturnValue(method = "canStart", at = @At("RETURN"))
    public boolean advanced_skills$canStart(boolean original) {
        return !ModEffectsKt.isSilenced(this.blaze) && original;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$tick(CallbackInfo ci) {
        if (ModEffectsKt.isSilenced(this.blaze)) {
            this.stop();
            ci.cancel();
        }
    }
}
