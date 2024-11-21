package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.init.ModEffectsKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WardenEntity.class)
public class WardenEntityMixin {

    @Inject(method = "isValidTarget", at = @At("RETURN"), cancellable = true)
    public void advanced_skills_re$isValidTarget(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ModEffectsKt.isSilenced((WardenEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addDarknessToClosePlayers", at = @At("HEAD"), cancellable = true)
    private static void advanced_skills_re$addDarknessToClosePlayers(ServerWorld world, Vec3d pos, Entity entity, int range, CallbackInfo ci) {
        if (entity instanceof LivingEntity living && ModEffectsKt.isSilenced(living)) {
            ci.cancel();
        }
    }
}
