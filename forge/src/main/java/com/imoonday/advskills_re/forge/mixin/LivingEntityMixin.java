package com.imoonday.advskills_re.forge.mixin;

import com.imoonday.advskills_re.api.AllowDeathEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract boolean isDead();

    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal = 1))
    private boolean advskills_re$beforeEntityKilled(LivingEntity livingEntity, DamageSource source, float amount) {
        return isDead() && (!(livingEntity instanceof ServerPlayerEntity player) || AllowDeathEvent.EVENT.invoker().allowDeath(player, source, amount));
    }
}
