package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class UnhinderedStrideSkill : Skill(
    Settings(
        id = "unhindered_stride",
        types = listOf(SkillType.ENHANCEMENT, SkillType.MOVEMENT),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), StepHeightTrigger, AutoStopTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = PARAM_STRIDE_DURATION,
            baseValue = DEFAULT_STRIDE_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT,
            genericText = true
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun getStepHeight(player: PlayerEntity): Float? =
        if (player.isUsing()) player.world.height.toFloat() else null

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_STRIDE_DURATION, player, DEFAULT_STRIDE_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {
        // Default Values
        private const val DEFAULT_STRIDE_DURATION = 10 * 20

        // Parameter Names
        private const val PARAM_STRIDE_DURATION = "stride_duration"  // 无阻行走持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}