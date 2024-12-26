package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class WaterWalkerSkill : Skill(
    id = "water_walker",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), WalkOnFluidTrigger, AutoStopTrigger, FluidMovementTrigger, UsingRenderTrigger {

    init {
        addEnhanceableParameter(timeParameterName, 15 * 20, "time", 0.2f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        user.playSound(SoundEvents.BLOCK_WATER_AMBIENT)
    }

    override fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean =
        player.isUsing() && state.isOf(Fluids.WATER) && player.getFluidHeight(FluidTags.WATER) < 0.02

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
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