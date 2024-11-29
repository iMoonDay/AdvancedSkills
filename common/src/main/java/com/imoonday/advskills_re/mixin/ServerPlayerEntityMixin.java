package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.api.LoopTaskContainer;
import com.imoonday.advskills_re.trigger.SkillTriggerHandler;
import com.imoonday.advskills_re.util.LoopTask;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements LoopTaskContainer {

    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();
    @Unique
    private final Map<Integer, LoopTask> tasks = new HashMap<>();
    @Unique
    private int taskId = 0;

    @Override
    public int addTask(LoopTask loopTask) {
        int id = taskId++;
        tasks.put(id, loopTask);
        return id;
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
    }

    @Override
    public @Nullable LoopTask getTask(int id) {
        return tasks.get(id);
    }

    @Override
    public void clearTasks() {
        tasks.clear();
        taskId = 0;
    }

    @Override
    public List<LoopTask> getTasks() {
        return List.copyOf(tasks.values());
    }

    @Override
    public void tickTasks() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        Iterator<Map.Entry<Integer, LoopTask>> iterator = tasks.entrySet().stream().sorted(Comparator.comparingInt(a -> -a.getValue().getPriority())).iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, LoopTask> entry = iterator.next();
            int id = entry.getKey();
            LoopTask task = entry.getValue();
            boolean shouldContinue = false;
            try {
                shouldContinue = task.tick(player);
            } catch (Exception e) {
                LOGGER.error("Error while ticking loop task {} for player {}", id, player.getDisplayName().getString(), e);
            }
            if (!shouldContinue) {
                iterator.remove();
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void advskills_re$tick(CallbackInfo ci) {
        tickTasks();
    }

    @Inject(method = "onLanding", at = @At("HEAD"))
    public void advskills_re$onLanding(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            SkillTriggerHandler.onLanding(player, player.fallDistance);
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), cancellable = true)
    private void advskills_re$damage$ignoreDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        Entity attacker = source.getAttacker();
        if (attacker == null) {
            attacker = source.getSource();
        }
        if (SkillTriggerHandler.ignoreDamage(amount, source, player, attacker)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void advskills_re$damage$postAttacked(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (cir.getReturnValue()) {
            LivingEntity attacker = null;
            if (source.getAttacker() instanceof LivingEntity entity) {
                attacker = entity;
            } else if (source.getSource() instanceof LivingEntity entity) {
                attacker = entity;
            }
            SkillTriggerHandler.postAttacked(source, player, attacker);
        }
    }
}
