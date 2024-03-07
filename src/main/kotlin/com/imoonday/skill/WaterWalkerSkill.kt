package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.FluidMovementTrigger
import com.imoonday.trigger.WalkOnFluidTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.translateSkill
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
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB,
    sound = SoundEvents.BLOCK_WATER_AMBIENT
), WalkOnFluidTrigger, AutoStopTrigger, FluidMovementTrigger {
    override fun getPersistTime(): Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult {
        val active = user.toggleUsing()
        if (!active) user.startCooling()
        return UseResult.consume(
            translateSkill(
                "wall_climbing", if (active) "active" else "inactive",
                name.string
            )
        )
    }

    override fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean =
        player.isUsing() && state.isOf(Fluids.WATER) && !player.isSubmergedInWater

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        super.onStop(player)
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean =
        player.isUsing() && tag == FluidTags.WATER && !player.isSubmergedInWater

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (!player.isUsing() || tag != FluidTags.WATER) speed else 0.0
}