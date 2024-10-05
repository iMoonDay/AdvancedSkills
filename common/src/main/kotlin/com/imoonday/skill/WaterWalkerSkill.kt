package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.FluidMovementTrigger
import com.imoonday.trigger.WalkOnFluidTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.toBlockPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.registry.tag.FluidTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class WaterWalkerSkill : Skill(
    id = "water_walker",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB,
    sound = SoundEvents.BLOCK_WATER_AMBIENT
), WalkOnFluidTrigger, AutoStopTrigger, FluidMovementTrigger {

    override fun getPersistTime(): Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean =
        player.isUsing() && state.isOf(Fluids.WATER) && player.getFluidHeight(FluidTags.WATER) < 0.02

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        super.onStop(player)
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean {
        val isOnWater = player.world.getFluidState(player.blockPos)
            .isIn(FluidTags.WATER) && player.world.getFluidState(player.eyePos.toBlockPos()).isEmpty
        val fluidHeight = player.world.getFluidState(player.blockPos).height - (player.y - player.blockY)
        return player.isUsing() && tag == FluidTags.WATER && (isOnWater && fluidHeight < 0.02)
    }

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (!player.isUsing() || tag != FluidTags.WATER) speed else 0.0
}