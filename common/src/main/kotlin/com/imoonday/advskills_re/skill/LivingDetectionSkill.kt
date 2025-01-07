package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class LivingDetectionSkill : Skill(
    Settings(
        id = "living_detection",
        types = listOf(SkillType.UTILITY),
        cooldown = 20,
        rarity = SkillRarity.SUPERB
    )
), AutoStopTrigger, GlowingTrigger {

    init {
        settings
            .addParameter(
                name = PARAM_DETECT_DURATION,
                baseValue = DEFAULT_DETECT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DETECT_RANGE,
                baseValue = DEFAULT_DETECT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean {
        val range = getDoubleParam(PARAM_DETECT_RANGE, clientPlayer, DEFAULT_DETECT_RANGE)
        return clientPlayer.isUsing()
            && entity != clientPlayer
            && entity.isLiving
            && entity.isAlive
            && clientPlayer.distanceTo(entity) <= range
            && (entity.x != entity.prevX
            || entity.y != entity.prevY
            || entity.z != entity.prevZ)
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_DETECT_DURATION, player, DEFAULT_DETECT_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {
        // Default Values
        private const val DEFAULT_DETECT_DURATION = 5 * 20
        private const val DEFAULT_DETECT_RANGE = 50.0

        // Parameter Names
        private const val PARAM_DETECT_DURATION = "detect_duration"  // 侦测持续时间
        private const val PARAM_DETECT_RANGE = "detect_range"  // 侦测范围

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_RANGE = "range"  // 对应侦测范围
    }
}