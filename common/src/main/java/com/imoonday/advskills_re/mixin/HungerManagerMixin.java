package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.skill.trigger.SkillTriggerHandler;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Shadow
    private int foodLevel;

    @Shadow
    private int prevFoodLevel;

    @Inject(method = "update", at = @At("HEAD"))
    private void advskills_re$OnUpdate(PlayerEntity player, CallbackInfo ci) {
        advskills_re$handleUpdate(player);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void advskills_re$postUpdate(PlayerEntity player, CallbackInfo ci) {
        advskills_re$handleUpdate(player);
    }

    @Unique
    private void advskills_re$handleUpdate(PlayerEntity player) {
        if (this.foodLevel != this.prevFoodLevel) {
            int level = SkillTriggerHandler.onFoodLevelChange(player, this.foodLevel);
            if (level != this.foodLevel) {
                this.foodLevel = level;
            }
        }
    }
} 