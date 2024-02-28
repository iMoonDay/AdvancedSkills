package com.imoonday.mixin;

import com.imoonday.components.EntityStatusComponentKt;
import com.imoonday.components.UsingSkillComponentKt;
import com.imoonday.init.ModEffectsKt;
import com.imoonday.skills.Skills;
import com.imoonday.triggers.SkillTriggerHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow
    private Optional<BlockPos> climbingPos;

    @ModifyReturnValue(method = "modifyAppliedDamage", at = @At("RETURN"))
    private float advanced_skills$modifyAppliedDamage(float original, DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || original <= 0.0f) return original;
        float newAmount = original;
        ServerPlayerEntity player = null;
        if (source.getSource() instanceof ServerPlayerEntity entity) {
            player = entity;
        }
        if (source.getSource() instanceof ProjectileEntity) {
            if (source.getAttacker() instanceof ServerPlayerEntity entity) {
                player = entity;
            }
        }
        LivingEntity living = (LivingEntity) (Object) this;
        if (player == null) {
            return living instanceof ServerPlayerEntity serverPlayer ? SkillTriggerHandler.INSTANCE.onPlayerDamaged(original, source, serverPlayer, source.getSource() instanceof LivingEntity entity ? entity : source.getAttacker() instanceof LivingEntity entity1 ? entity1 : null) : original;
        }
        newAmount = SkillTriggerHandler.INSTANCE.onAttack(newAmount, source, player, living);
        newAmount = living instanceof ServerPlayerEntity serverPlayer ? SkillTriggerHandler.INSTANCE.onPlayerDamaged(newAmount, source, serverPlayer, player) : SkillTriggerHandler.INSTANCE.onDamaged(newAmount, source, living, player);
        return newAmount;
    }

    @ModifyReturnValue(method = "computeFallDamage", at = @At("RETURN"))
    private int advanced_skills$computeFallDamage(int original, float fallDistance, float damageMultiplier) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (original <= 0.0f || !(living instanceof ServerPlayerEntity player)) return original;
        return SkillTriggerHandler.INSTANCE.onFall(original, player, fallDistance, damageMultiplier);
    }

    @ModifyReturnValue(method = "isClimbing", at = @At("RETURN"))
    private boolean advanced_skills$isClimbing(boolean original) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (living instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.allowClimbing(player)) {
            this.climbingPos = Optional.of(living.getBlockPos());
            return true;
        }
        return original;
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void advanced_skills$tickMovement(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (ModEffectsKt.isForceFrozen(living) || ModEffectsKt.isConfined(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setHeadYaw", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$setHeadYaw(float headYaw, CallbackInfo ci) {
        if (ModEffectsKt.isForceFrozen((LivingEntity) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "setBodyYaw", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$setBodyYaw(float bodyYaw, CallbackInfo ci) {
        if (ModEffectsKt.isForceFrozen((LivingEntity) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void advanced_skills$tick(CallbackInfo ci) {
        EntityStatusComponentKt.syncStatus((LivingEntity) (Object) this);
    }

    @ModifyReturnValue(method = "hasStatusEffect", at = @At("RETURN"))
    public boolean advanced_skills$hasStatusEffect(boolean original, StatusEffect effect) {
        if (effect != StatusEffects.NIGHT_VISION) return original;
        return (LivingEntity) (Object) this instanceof PlayerEntity player && UsingSkillComponentKt.isUsingSkill(player, Skills.NIGHT_VISION) || original;
    }
}
