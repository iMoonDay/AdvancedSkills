package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.skill.trigger.SkillTriggerHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Comparator;
import java.util.Optional;

@Mixin(ActiveTargetGoal.class)
public class ActiveTargetGoalMixin {

    @Redirect(method = "findClosestTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/ai/TargetPredicate;Lnet/minecraft/entity/LivingEntity;DDD)Lnet/minecraft/entity/player/PlayerEntity;"))
    private PlayerEntity findClosestPlayer(World instance, TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
        Optional<? extends PlayerEntity> closestTaunter = instance.getPlayers().stream().filter(player -> targetPredicate.test(entity, player) && SkillTriggerHandler.isTaunter(player)).min(Comparator.comparingDouble(player -> player.squaredDistanceTo(x, y, z)));
        if (closestTaunter.isPresent()) {
            return closestTaunter.get();
        }
        return instance.getClosestPlayer(targetPredicate, entity, x, y, z);
    }
}
