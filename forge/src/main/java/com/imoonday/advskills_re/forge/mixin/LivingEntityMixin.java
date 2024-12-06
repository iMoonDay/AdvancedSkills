package com.imoonday.advskills_re.forge.mixin;

import com.imoonday.advskills_re.api.AllowDeathEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract boolean isDead();

    @Unique
    private boolean advskills_re$keepAlive;

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal = 1))
    private void advskills_re$beforeEntityKilled(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (isDead() && entity instanceof ServerPlayerEntity player) {
            if (!AllowDeathEvent.EVENT.invoker().allowDeath(player, source, amount)) {
                if (entity.getHealth() <= 0.0F) {
                    entity.setHealth(1.0F);
                }
                advskills_re$keepAlive = true;
            }
        } else if (advskills_re$keepAlive) {
            advskills_re$keepAlive = false;
        }
    }

    @Inject(method = "isDead", at = @At(value = "HEAD"), cancellable = true)
    private void advskills_re$keepAlive(CallbackInfoReturnable<Boolean> cir) {
        if (advskills_re$keepAlive) {
            advskills_re$keepAlive = false;
            cir.setReturnValue(false);
        }
    }
}
