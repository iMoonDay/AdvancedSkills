package com.imoonday.advanced_skills_re.mixin;

import com.imoonday.init.ModEffectsKt;
import com.imoonday.trigger.SkillTriggerHandler;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    private Optional<BlockPos> climbingPos;

    @Shadow
    protected int itemUseTimeLeft;

    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"), cancellable = true)
    private void advanced_skills_re$modifyAppliedDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
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
                newAmount = SkillTriggerHandler.INSTANCE.onDamaged(newAmount, source, player, attacker);
            }
            if (attacker instanceof ServerPlayerEntity player) {
                newAmount = SkillTriggerHandler.INSTANCE.onAttack(newAmount, source, player, target);
            }
            cir.setReturnValue(newAmount);
        }
    }

    @Inject(method = "computeFallDamage", at = @At("RETURN"), cancellable = true)
    private void advanced_skills_re$computeFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        int original = cir.getReturnValue();
        if (original > 0.0f && entity instanceof ServerPlayerEntity player) {
            cir.setReturnValue(SkillTriggerHandler.INSTANCE.onFall(original, player, fallDistance, damageMultiplier));
        }
    }

    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void advanced_skills_re$isClimbing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.allowClimbing(player)) {
            this.climbingPos = Optional.of(entity.getBlockPos());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void advanced_skills_re$tickMovement(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ModEffectsKt.isForceFrozen(entity) || ModEffectsKt.isConfined(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setHeadYaw", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$setHeadYaw(float headYaw, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ModEffectsKt.isForceFrozen(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setBodyYaw", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$setBodyYaw(float bodyYaw, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ModEffectsKt.isForceFrozen(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "hasStatusEffect", at = @At("RETURN"), cancellable = true)
    public void advanced_skills_re$hasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!cir.getReturnValue() && effect == StatusEffects.NIGHT_VISION && entity instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.hasNightVision(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$canWalkOnFluid(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.allowWalkOnFluid(player, fluidState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$canBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.canBreatheInWater(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setCurrentHand", at = @At("TAIL"))
    public void advanced_skills_re$setCurrentHand(Hand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            ItemStack itemStack = this.getStackInHand(hand);
            float multiplier = SkillTriggerHandler.INSTANCE.getItemMaxUseTimeMultiplier(player, itemStack);
            this.itemUseTimeLeft = (int) (this.itemUseTimeLeft * multiplier);
        }
    }

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    public void advanced_skills_re$canHaveStatusEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.cannotHaveStatusEffect(player, effect)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
    private void advanced_skills_re$getJumpVelocity(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.shouldInvertJump(player)) {
            cir.setReturnValue(-cir.getReturnValue());
        }
    }
}
