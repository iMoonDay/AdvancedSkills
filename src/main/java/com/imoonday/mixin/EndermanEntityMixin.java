package com.imoonday.mixin;

import com.imoonday.init.ModEffectsKt;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin {

    @Inject(method = "teleportRandomly", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$teleportRandomly(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isSilenced((EndermanEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$teleportToEntity(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isSilenced((EndermanEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "teleportTo(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$teleportToPos(CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isSilenced((EndermanEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
