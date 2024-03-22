package com.imoonday.mixin;

import com.imoonday.init.ModEffectsKt;
import com.imoonday.skill.Skills;
import com.imoonday.trigger.SkillTriggerHandler;
import com.imoonday.util.PlayerUtilsKt;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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

    @ModifyReturnValue(method = "modifyAppliedDamage", at = @At("RETURN"))
    private float advanced_skills$modifyAppliedDamage(float original, DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || original <= 0.0f) return original;
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

    @ModifyReturnValue(method = "hasStatusEffect", at = @At("RETURN"))
    public boolean advanced_skills$hasStatusEffect(boolean original, StatusEffect effect) {
        if (effect != StatusEffects.NIGHT_VISION) return original;
        return (LivingEntity) (Object) this instanceof PlayerEntity player && PlayerUtilsKt.isUsing(player, Skills.NIGHT_VISION) || original;
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$canWalkOnFluid(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.allowWalkOnFluid(player, fluidState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$canBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.canBreatheInWater(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setCurrentHand", at = @At("TAIL"))
    public void advanced_skills$setCurrentHand(Hand hand, CallbackInfo ci) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
            ItemStack itemStack = this.getStackInHand(hand);
            float multiplier = SkillTriggerHandler.INSTANCE.getItemMaxUseTimeMultiplier(player, itemStack);
            this.itemUseTimeLeft = (int) (this.itemUseTimeLeft * multiplier);
        }
    }

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$canHaveStatusEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.cannotHaveStatusEffect(player, effect)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "getJumpVelocity", at = @At("RETURN"))
    private float advanced_skills$getJumpVelocity(float original) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player && SkillTriggerHandler.INSTANCE.shouldInvertJump(player)) {
            return -original;
        }
        return original;
    }
}
