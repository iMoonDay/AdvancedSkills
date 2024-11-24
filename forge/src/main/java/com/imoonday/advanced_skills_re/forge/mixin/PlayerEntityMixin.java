package com.imoonday.advanced_skills_re.forge.mixin;

import com.imoonday.trigger.SkillTriggerHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> arg, World arg2) {
        super(arg, arg2);
        throw new AssertionError("Mixin constructor called!");
    }

    @Override
    public boolean canSwimInFluidType(FluidType type) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (type == ForgeMod.WATER_TYPE.get() && SkillTriggerHandler.INSTANCE.ignoreFluid(player, FluidTags.WATER)) {
            return false;
        }
        return super.canSwimInFluidType(type);
    }
}
