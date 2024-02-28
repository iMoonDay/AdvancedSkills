package com.imoonday.mixin;

import com.imoonday.triggers.SkillTriggerHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "postMine", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void advanced_skills$postMine(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo ci) {
        SkillTriggerHandler.INSTANCE.postMine(world, state, pos, miner, (ItemStack) (Object) this);
    }

    @Inject(method = "postHit", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void advanced_skills$postHit(LivingEntity target, PlayerEntity attacker, CallbackInfo ci) {
        SkillTriggerHandler.INSTANCE.postHit(target, attacker, (ItemStack) (Object) this);
    }
}
