package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.component.startCooling
import com.imoonday.component.toggleUsingSkill
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
    override val persistTime: Int = 20 * 15
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult {
        val active = user.toggleUsingSkill(this)
        if (!active) user.startCooling(this)
        return UseResult.consume(
            translateSkill(
                "wall_climbing", if (active) "active" else "inactive",
                name.string
            )
        )
    }

    override fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean =
        player.isUsingSkill(this) && state.isOf(Fluids.WATER) && !player.isSubmergedInWater

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling(this)
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean =
        player.isUsingSkill(this) && tag == FluidTags.WATER && !player.isSubmergedInWater

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (!player.isUsingSkill(this) || tag != FluidTags.WATER) speed else 0.0
}