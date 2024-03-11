package com.imoonday.mixin;

import com.imoonday.init.ModEffectsKt;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.mob.GhastEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.entity.mob.GhastEntity$ShootFireballGoal")
public abstract class GhastEntity$ShootFireballGoalMixin {

    @Shadow
    @Final
    private GhastEntity ghast;

    @Shadow
    public abstract void stop();

    @ModifyReturnValue(method = "canStart", at = @At("RETURN"))
    public boolean advanced_skills$canStart(boolean original) {
        return !ModEffectsKt.isSilenced(this.ghast) && original;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$tick(CallbackInfo ci) {
        if (ModEffectsKt.isSilenced(this.ghast)) {
            this.stop();
            ci.cancel();
        }
    }
}
