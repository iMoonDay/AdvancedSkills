package com.imoonday.mixin;

import com.imoonday.entity.Servant;
import com.imoonday.trigger.SkillTriggerHandler;
import com.imoonday.util.PlayerUtilsKt;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow
    protected abstract boolean clipAtLedge();

    @Shadow
    protected abstract boolean method_30263();

    @Inject(method = "addExperience", at = @At("TAIL"))
    public void advanced_skills$addExperience(int experience, CallbackInfo ci) {
        if (experience > 0) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            PlayerUtilsKt.setSkillExp(player, PlayerUtilsKt.getSkillExp(player) + experience);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void advanced_skills$isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Servant.Companion.invulnerableToServant(damageSource, cir, player);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void advanced_skills$tick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            SkillTriggerHandler.INSTANCE.serverTick(serverPlayer);
        }
        SkillTriggerHandler.INSTANCE.tick(player);
    }

    @ModifyReturnValue(method = "getActiveEyeHeight", at = @At("RETURN"))
    private float advanced_skills$getActiveEyeHeight(float original, EntityPose pose, EntityDimensions dimensions) {
        return SkillTriggerHandler.INSTANCE.getEyeHeight((PlayerEntity) (Object) this, original, pose, dimensions);
    }

    @Inject(method = "adjustMovementForSneaking", at = @At("HEAD"), cancellable = true)
    private void advanced_skills$adjustMovementForSneaking(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (SkillTriggerHandler.INSTANCE.shouldInvertSneak(player)) {
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
}
