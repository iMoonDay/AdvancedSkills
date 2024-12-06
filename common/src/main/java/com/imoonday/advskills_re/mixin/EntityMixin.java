package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.api.ICollisionRecorder;
import com.imoonday.advskills_re.api.Propertied;
import com.imoonday.advskills_re.client.ClientTriggerHandler;
import com.imoonday.advskills_re.component.EntityPropertyComponent;
import com.imoonday.advskills_re.init.ModEffectsKt;
import com.imoonday.advskills_re.skill.trigger.SkillTriggerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements Propertied, ICollisionRecorder {

    @Shadow
    private float stepHeight;

    @Shadow public boolean horizontalCollision;
    @Shadow public boolean verticalCollision;
    @Shadow public boolean groundCollision;
    @Unique
    private EntityPropertyComponent propertyComponent;
    @Unique
    private boolean wasHorizontalCollision;
    @Unique
    private boolean wasVerticalCollision;
    @Unique
    private boolean wasGroundCollision;

    @Override
    public EntityPropertyComponent getPropertyComponent() {
        if (propertyComponent == null) {
            propertyComponent = new EntityPropertyComponent((Entity) (Object) this);
        }
        return propertyComponent;
    }

    @Override
    public boolean wasHorizontalCollision() {
        return wasHorizontalCollision;
    }

    @Override
    public boolean wasVerticalCollision() {
        return wasVerticalCollision;
    }

    @Override
    public boolean wasGroundCollision() {
        return wasGroundCollision;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.AFTER))
    public void advskills_re$move(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        this.wasHorizontalCollision = this.horizontalCollision;
        this.wasVerticalCollision = this.verticalCollision;
        this.wasGroundCollision = this.groundCollision;
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void advskills_re$changeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setYaw", at = @At("HEAD"), cancellable = true)
    public void advskills_re$setYaw(float yaw, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setHeadYaw", at = @At("HEAD"), cancellable = true)
    public void advskills_re$setHeadYaw(float headYaw, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setBodyYaw", at = @At("HEAD"), cancellable = true)
    public void advskills_re$setBodyYaw(float bodyYaw, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setPitch", at = @At("HEAD"), cancellable = true)
    public void advskills_re$setPitch(float pitch, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity living && ModEffectsKt.isForceFrozen(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "getStepHeight", at = @At("HEAD"), cancellable = true)
    private void advskills_re$getStepHeight(CallbackInfoReturnable<Float> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            Float height = SkillTriggerHandler.getStepHeight(player);
            if (height != null && height > this.stepHeight) {
                cir.setReturnValue(height);
            }
        }
    }

    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    private void advskills_re$updateMovementInFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.ignoreFluid(player, tag)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isSubmergedIn", at = @At("HEAD"), cancellable = true)
    private void advskills_re$isSubmergedIn(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.ignoreFluid(player, tag)) {
                cir.setReturnValue(false);
            }
        }
    }

    @ModifyVariable(method = "updateMovementInFluid", at = @At("HEAD"), argsOnly = true, index = 2)
    private double advskills_re$modifySpeed(double value, TagKey<Fluid> tag, double speed) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            return SkillTriggerHandler.getMovementInFluid(player, tag, value);
        }
        return speed;
    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void advskills_re$isInvisible(CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.isInvisible(player) || SkillTriggerHandler.isDisguising(player)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void advskills_re$isInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity entity) {
            if (!SkillTriggerHandler.isInvisibleTo(entity, player) || SkillTriggerHandler.isDisguising(player)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void advskills_re$isGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity.getWorld().isClient && ClientTriggerHandler.isGlowing(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
    private void advskills_re$isInLava(CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player) {
            if (SkillTriggerHandler.ignoreLava(player)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
    private void advskills_re$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("entityPropertyComponent")) {
            getPropertyComponent().readFromNbt(nbt.getCompound("entityPropertyComponent"));
        }
    }

    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
    private void advskills_re$writeCustomDataToNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        nbt.put("entityPropertyComponent", getPropertyComponent().toNbt());
    }
}
