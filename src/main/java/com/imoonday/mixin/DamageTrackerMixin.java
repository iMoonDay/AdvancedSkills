package com.imoonday.mixin;

import com.imoonday.components.DamagedTimeComponentKt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {

    @Shadow
    @Final
    private LivingEntity entity;

    @Inject(method = "onDamage", at = @At("HEAD"))
    public void advanced_skills$onDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayerEntity player) {
            DamagedTimeComponentKt.onDamage(player);
        }
    }
}
