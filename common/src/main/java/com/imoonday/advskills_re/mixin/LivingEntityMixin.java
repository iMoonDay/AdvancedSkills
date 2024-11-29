package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.effect.SeriousInjuryEffect;
import com.imoonday.advskills_re.init.ModEffectsKt;
import com.imoonday.advskills_re.trigger.SkillTriggerHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {

    @Shadow
    private Optional<BlockPos> climbingPos;

    @Shadow
    protected int itemUseTimeLeft;

    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);

    @Shadow
    public abstract boolean isDead();

    @Shadow
    public abstract float getMaxHealth();

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"), cancellable = true)
    private void advskills_re$modifyAppliedDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        Float original = cir.getReturnValue();
        if (!source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && original > 0.0f) {
            LivingEntity attacker = null;
            if (source.getAttacker() instanceof LivingEntity entity) {
                attacker = entity;
            } else if (source.getSource() instanceof LivingEntity entity) {
                attacker = entity;
            }
            float newAmount = original;
            LivingEntity target = (LivingEntity) (Object) this;
            if (target instanceof ServerPlayerEntity player) {
                newAmount = SkillTriggerHandler.onDamaged(newAmount, source, player, attacker);
            }
            if (attacker instanceof ServerPlayerEntity player) {
                newAmount = SkillTriggerHandler.onAttack(newAmount, source, player, target);
            }
            cir.setReturnValue(newAmount);
        }
    }

    @Inject(method = "computeFallDamage", at = @At("RETURN"), cancellable = true)
    private void advskills_re$computeFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        int original = cir.getReturnValue();
        if (original > 0.0f && entity instanceof ServerPlayerEntity player) {
            cir.setReturnValue(SkillTriggerHandler.onFall(original, player, fallDistance, damageMultiplier));
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void advskills_re$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (cir.getReturnValue()) {
            ServerPlayerEntity player = null;
            if (source.getAttacker() instanceof ServerPlayerEntity entity) {
                player = entity;
            } else if (source.getSource() instanceof ServerPlayerEntity entity) {
                player = entity;
            }
            if (player != null) {
                SkillTriggerHandler.postAttack(source, player, target);
            }
        }
    }

    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void advskills_re$isClimbing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.allowClimbing(player)) {
            this.climbingPos = Optional.of(entity.getBlockPos());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void advskills_re$tickMovement(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ModEffectsKt.isForceFrozen(entity) || ModEffectsKt.isConfined(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setHeadYaw", at = @At("HEAD"), cancellable = true)
    public void advskills_re$setHeadYaw(float headYaw, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ModEffectsKt.isForceFrozen(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setBodyYaw", at = @At("HEAD"), cancellable = true)
    public void advskills_re$setBodyYaw(float bodyYaw, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ModEffectsKt.isForceFrozen(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "hasStatusEffect", at = @At("RETURN"), cancellable = true)
    public void advskills_re$hasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!cir.getReturnValue() && effect == StatusEffects.NIGHT_VISION && entity instanceof PlayerEntity player && SkillTriggerHandler.hasNightVision(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    public void advskills_re$canWalkOnFluid(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.allowWalkOnFluid(player, fluidState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    public void advskills_re$canBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.canBreatheInWater(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setCurrentHand", at = @At("TAIL"))
    public void advskills_re$setCurrentHand(Hand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            ItemStack itemStack = this.getStackInHand(hand);
            float multiplier = SkillTriggerHandler.getItemMaxUseTimeMultiplier(player, itemStack);
            this.itemUseTimeLeft = (int) (this.itemUseTimeLeft * multiplier);
        }
    }

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    public void advskills_re$canHaveStatusEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.cannotHaveStatusEffect(player, effect)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
    private void advskills_re$getJumpVelocity(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.shouldInvertJump(player)) {
            cir.setReturnValue(-cir.getReturnValue());
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    public void advskills_re$setHealth(float health, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        health = MathHelper.clamp(health, 0.0F, this.getMaxHealth());
        if (SeriousInjuryEffect.Companion.onSetHealth(entity, health)) {
            ci.cancel();
        }
    }

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateGlowing()V", shift = At.Shift.AFTER))
    public void advskills_re$tickStatusEffects(CallbackInfo ci) {
        getPropertyComponent().onEffectsChanged();
    }
}
