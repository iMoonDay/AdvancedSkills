package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class PrimaryFreezeSkill : Skill(
    Settings(
        id = "primary_freeze",
        types = listOf(SkillType.CONTROL),
        cooldown = 8,
        rarity = SkillRarity.SUPERB
    )
), SpecialStateRenderTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_FREEZE_SOUND, DEFAULT_FREEZE_SOUND)
            .addParameter(
                name = PARAM_FREEZE_RANGE,
                baseValue = DEFAULT_FREEZE_RANGE,
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
        val range = getIntParam(PARAM_FREEZE_RANGE, user, DEFAULT_FREEZE_RANGE)
        val count = getIntParam(PARAM_BALL_COUNT, user, DEFAULT_BALL_COUNT)
        val immune = getBooleanParam(PARAM_SELF_IMMUNE, user, DEFAULT_SELF_IMMUNE)
        user.executeAndAddTask(5, count) { user.spawnEnergyBall(range, immune) }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnEnergyBall(extraRange: Int, ignoreSelf: Boolean): Boolean {
        val rotation = rotationVector.normalize().multiply(1.5)
        return world.spawnEntity(
            FreezeEnergyBallEntity(
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
                playSoundFromParam(PARAM_FREEZE_SOUND, DEFAULT_FREEZE_SOUND.get())
            }
        }
    }

    override fun isInSpecialState(player: PlayerEntity): Boolean = player.isForceFrozen

    companion object {

        // Default Values
        private const val DEFAULT_FREEZE_RANGE = 0
        private const val DEFAULT_BALL_COUNT = 1
        private const val DEFAULT_SELF_IMMUNE = false
        private val DEFAULT_FREEZE_SOUND = ModSounds.FIRE

        // Parameter Names
        private const val PARAM_FREEZE_SOUND = "freeze_sound"  // 冰冻音效
        private const val PARAM_FREEZE_RANGE = "freeze_range"  // 冰冻范围
        private const val PARAM_BALL_COUNT = "ball_count"  // 能量球数量
        private const val PARAM_SELF_IMMUNE = "self_immune"  // 自身免疫

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应范围
        private const val ENHANCEMENT_COUNT = "count"  // 对应数量
        private const val ENHANCEMENT_IMMUNE = "self_immune"  // 对应自身免疫
    }
}