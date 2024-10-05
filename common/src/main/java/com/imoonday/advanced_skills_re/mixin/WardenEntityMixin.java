package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.init.ModEffectsKt;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenEntity.class)
public class WardenEntityMixin {

    @ModifyReturnValue(method = "isValidTarget", at = @At("RETURN"))
    public boolean advanced_skills$isValidTarget(boolean original) {
        return !ModEffectsKt.isSilenced((WardenEntity) (Object) this) && original;
    }

    @Inject(method = "addDarknessToClosePlayers", at = @At("HEAD"), cancellable = true)
    private static void advanced_skills$addDarknessToClosePlayers(ServerWorld world, Vec3d pos, Entity entity, int range, CallbackInfo ci) {
        if (entity instanceof LivingEntity living && ModEffectsKt.isSilenced(living)) {
            ci.cancel();
        }
    }
}
