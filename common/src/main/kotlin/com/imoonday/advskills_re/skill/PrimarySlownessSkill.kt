package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class PrimarySlownessSkill : Skill(
    Settings(
        id = "primary_slowness",
        types = listOf(SkillType.CONTROL),
        cooldown = 6,
        rarity = SkillRarity.RARE
    )
) {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_SLOW_SOUND, DEFAULT_SLOW_SOUND)
            .addParameter(
                name = PARAM_SLOW_RANGE,
                baseValue = DEFAULT_SLOW_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_BALL_COUNT,
                baseValue = DEFAULT_BALL_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_SELF_IMMUNE,
                baseValue = DEFAULT_SELF_IMMUNE,
                enhancementId = ENHANCEMENT_IMMUNE
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val range = getIntParam(PARAM_SLOW_RANGE, user, DEFAULT_SLOW_RANGE)
        val count = getIntParam(PARAM_BALL_COUNT, user, DEFAULT_BALL_COUNT)
        val immune = getBooleanParam(PARAM_SELF_IMMUNE, user, DEFAULT_SELF_IMMUNE)
        user.executeAndAddTask(5, count) { user.spawnEnergyBall(range, immune) }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnEnergyBall(extraRange: Int, ignoreSelf: Boolean): Boolean {
        val rotation = rotationVector.normalize().multiply(1.5)
        return world.spawnEntity(
            SlownessEnergyBallEntity(
                this,
                rotationVector.x,
                rotationVector.y,
                rotationVector.z,
                world
            ).apply {
                setPosition(x + rotation.x, eyeY, z + rotation.z)
                range += extraRange
                if (ignoreSelf) {
                    ignoreOwner = true
                }
            }
        ).also {
            if (it) {
                playSoundFromParam(PARAM_SLOW_SOUND, DEFAULT_SLOW_SOUND.get())
            }
        }
    }

    companion object {

        // Default Values
        private const val DEFAULT_SLOW_RANGE = 0
        private const val DEFAULT_BALL_COUNT = 1
        private const val DEFAULT_SELF_IMMUNE = false
        private val DEFAULT_SLOW_SOUND = ModSounds.FIRE

        // Parameter Names
        private const val PARAM_SLOW_SOUND = "slow_sound"  // 减速音效
        private const val PARAM_SLOW_RANGE = "slow_range"  // 减速范围
        private const val PARAM_BALL_COUNT = "ball_count"  // 能量球数量
        private const val PARAM_SELF_IMMUNE = "self_immune"  // 自身免疫

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应范围
        private const val ENHANCEMENT_COUNT = "count"  // 对应数量
        private const val ENHANCEMENT_IMMUNE = "self_immune"  // 对应自身免疫
    }
}