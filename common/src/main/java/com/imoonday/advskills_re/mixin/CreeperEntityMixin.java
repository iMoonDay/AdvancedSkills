package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.init.ModEffectsKt;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {

    @Shadow
    private int currentFuseTime;

    @Shadow
    public abstract void setFuseSpeed(int fuseSpeed);

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    public void advskills_re$explode(CallbackInfo ci) {
        if (ModEffectsKt.isSilenced((CreeperEntity) (Object) this)) {
            this.setFuseSpeed(-1);
            this.currentFuseTime -= 1;
            ci.cancel();
        }
    }
}
