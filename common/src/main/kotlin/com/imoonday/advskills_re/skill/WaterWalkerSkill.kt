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

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_WALK_SOUND, DEFAULT_WALK_SOUND)
            .addParameter(
                name = PARAM_WALK_DURATION,
                baseValue = DEFAULT_WALK_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        user.playSoundFromParam(PARAM_WALK_SOUND, DEFAULT_WALK_SOUND)
    }

    override fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean =
        player.isUsing() && state.isOf(Fluids.WATER) && player.getFluidHeight(FluidTags.WATER) < FLUID_HEIGHT_THRESHOLD

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_WALK_DURATION, player, DEFAULT_WALK_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean {
        val isOnWater = player.world.getFluidState(player.blockPos)
            .isIn(FluidTags.WATER) && player.world.getFluidState(player.eyePos.toBlockPos()).isEmpty
        val fluidHeight = player.world.getFluidState(player.blockPos).height - (player.y - player.blockY)
        return player.isUsing() && tag == FluidTags.WATER && (isOnWater && fluidHeight < FLUID_HEIGHT_THRESHOLD)
    }

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (!player.isUsing() || tag != FluidTags.WATER) speed else 0.0

    companion object {

        // Default Values
        private const val DEFAULT_WALK_DURATION = 15 * 20
        private val DEFAULT_WALK_SOUND = SoundEvents.BLOCK_WATER_AMBIENT
        private const val FLUID_HEIGHT_THRESHOLD = 0.02  // 流体高度阈值

        // Parameter Names
        private const val PARAM_WALK_SOUND = "walk_sound"  // 行走音效
        private const val PARAM_WALK_DURATION = "walk_duration"  // 行走持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}