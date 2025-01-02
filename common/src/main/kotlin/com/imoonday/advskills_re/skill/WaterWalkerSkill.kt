package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
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
    Settings(
        id = "water_walker",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), WalkOnFluidTrigger, AutoStopTrigger, FluidMovementTrigger, UsingRenderTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("use_sound", SoundEvents.BLOCK_WATER_AMBIENT)
            .addParameter(
                name = timeParamName,
                baseValue = 15 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        user.playSoundFromParam("use_sound", SoundEvents.BLOCK_WATER_AMBIENT)
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