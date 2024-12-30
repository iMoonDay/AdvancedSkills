package com.imoonday.advskills_re.mixin;

import com.imoonday.advskills_re.api.DamageFilter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(LightningEntity.class)
public class LightningEntityMixin implements DamageFilter {

    @Unique
    private Predicate<Entity> advskills_re$noDamage = entity -> false;

    @Override
    public boolean passDamage(Entity entity) {
        return advskills_re$noDamage.test(entity);
    }

    @Override
    public void setNoDamagePredicate(Predicate<Entity> predicate) {
        advskills_re$noDamage = predicate;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onStruckByLightning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LightningEntity;)V"))
    public void onStruckByLightning(Entity instance, ServerWorld world, LightningEntity lightning) {
        if (advskills_re$noDamage == null || !advskills_re$noDamage.test(instance)) {
            instance.onStruckByLightning(world, lightning);
        }
    }
}
