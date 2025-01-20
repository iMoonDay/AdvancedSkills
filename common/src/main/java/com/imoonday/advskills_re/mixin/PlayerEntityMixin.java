package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.api.PlayerDataContainer;
import com.imoonday.advskills_re.component.PlayerDataComponent;
import com.imoonday.advskills_re.config.GlobalConfig;
import com.imoonday.advskills_re.config.SkillConfig;
import com.imoonday.advskills_re.entity.Servant;
import com.imoonday.advskills_re.skill.trigger.SkillTriggerHandler;
import com.imoonday.advskills_re.util.PlayerUtilsKt;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerDataContainer {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        throw new AssertionError("Mixin constructor called!");
    }

    @Shadow
    protected abstract boolean clipAtLedge();

    @Shadow
    protected abstract boolean method_30263();

    @Unique
    private PlayerDataComponent dataComponent;

    @Override
    public PlayerDataComponent getDataComponent() {
        if (dataComponent == null) {
            dataComponent = new PlayerDataComponent((PlayerEntity) (Object) this);
            dataComponent.getChoiceData().setCount(GlobalConfig.get().getInitialDrawTimes());
        }
        return dataComponent;
    }

    @Inject(method = "addExperience", at = @At("TAIL"))
    public void advskills_re$addExperience(int experience, CallbackInfo ci) {
        if (experience > 0) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            Double multiplier = SkillConfig.get().getSkillXpMultiplier();
            if (multiplier == null) {
                multiplier = GlobalConfig.get().getSkillConfig().getSkillXpMultiplier();
            }
            if (multiplier != null && multiplier != 1.0) {
                experience = (int) (experience * multiplier);
            }
            PlayerUtilsKt.setSkillExp(player, PlayerUtilsKt.getSkillExp(player) + experience);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void advskills_re$isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Servant.Companion.invulnerableToServant(damageSource, cir, player);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void advskills_re$tick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        SkillTriggerHandler.playerTick(player);
    }

    @Inject(method = "getActiveEyeHeight", at = @At("RETURN"), cancellable = true)
    private void advskills_re$getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(SkillTriggerHandler.getEyeHeight((PlayerEntity) (Object) this, cir.getReturnValue(), pose, dimensions));
    }

    @Inject(method = "adjustMovementForSneaking", at = @At("HEAD"), cancellable = true)
    private void advskills_re$adjustMovementForSneaking(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (SkillTriggerHandler.shouldInvertSneak(player)) {
            if (!player.getAbilities().flying && movement.y >= 0.0 && (type == MovementType.SELF || type == MovementType.PLAYER) && this.clipAtLedge() && this.method_30263()) {
                double d = movement.x;
                double e = movement.z;
                double f = 0.05;
                while (d != 0.0 && player.getWorld().isSpaceEmpty(player, player.getBoundingBox().offset(d, player.getStepHeight(), 0.0))) {
                    if (d < f && d >= -f) {
                        d = 0.0;
                        continue;
                    }
                    if (d > 0.0) {
                        d -= f;
                        continue;
                    }
                    d += f;
                }
                while (e != 0.0 && player.getWorld().isSpaceEmpty(player, player.getBoundingBox().offset(0.0, player.getStepHeight(), e))) {
                    if (e < f && e >= -f) {
                        e = 0.0;
                        continue;
                    }
                    if (e > 0.0) {
                        e -= f;
                        continue;
                    }
                    e += f;
                }
                while (d != 0.0 && e != 0.0 && player.getWorld().isSpaceEmpty(player, player.getBoundingBox().offset(d, player.getStepHeight(), e))) {
                    d = d < f && d >= -f ? 0.0 : (d > 0.0 ? (d -= f) : (d += f));
                    if (e < f && e >= -f) {
                        e = 0.0;
                        continue;
                    }
                    if (e > 0.0) {
                        e -= f;
                        continue;
                    }
                    e += f;
                }
                cir.setReturnValue(new Vec3d(d, movement.y, e));
            }
        }
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;spawnSweepAttackParticles()V", shift = At.Shift.AFTER))
    private void advskills_re$attack(Entity target, CallbackInfo ci) {
        if (target instanceof LivingEntity entity) {
            SkillTriggerHandler.postSweepAttack((PlayerEntity) (Object) this, entity);
        }
    }

    @Inject(method = "checkFallFlying", at = @At("HEAD"), cancellable = true)
    private void advskills_re$checkFallFlying(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!SkillTriggerHandler.canStartFallFlying(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void advskills_re$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("playerDataComponent", getDataComponent().toNbt());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void advskills_re$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        PlayerDataComponent component = getDataComponent();
        if (nbt.contains("playerDataComponent")) {
            component.readFromNbt(nbt.getCompound("playerDataComponent"));
        }
    }
}
