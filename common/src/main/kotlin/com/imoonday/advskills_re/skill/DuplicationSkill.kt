package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.server.network.*

class DuplicationSkill : Skill(
    Settings(
        id = "duplication",
        types = listOf(SkillType.SUMMON),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
), SendPlayerVelocityTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_CLONE_INTERVAL, DEFAULT_CLONE_INTERVAL)
            .addParameter(PARAM_CLONE_MOVE_TIME, DEFAULT_CLONE_MOVE_TIME)
            .addParameter(
                name = PARAM_INVISIBLE_DURATION,
                baseValue = DEFAULT_INVISIBLE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_CLONE_COUNT,
                baseValue = DEFAULT_CLONE_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_ATTACK_HOSTILES,
                baseValue = DEFAULT_ATTACK_HOSTILES,
                enhancementId = ENHANCEMENT_ATTACK_HOSTILES
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val interval = getIntParam(PARAM_CLONE_INTERVAL, user, DEFAULT_CLONE_INTERVAL, 0)
        val amount = getIntParam(PARAM_CLONE_COUNT, user, DEFAULT_CLONE_COUNT)
        val time = getIntParam(PARAM_CLONE_MOVE_TIME, user, DEFAULT_CLONE_MOVE_TIME)
        val attackHostiles = getBooleanParam(PARAM_ATTACK_HOSTILES, user, DEFAULT_ATTACK_HOSTILES)
        user.executeAndAddTask(interval, amount) { summonClones(user, time, attackHostiles) }
        val duration = getIntParam(PARAM_INVISIBLE_DURATION, user, DEFAULT_INVISIBLE_DURATION)
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, true, false, true))
        return UseResult.success()
    }

    private fun summonClones(user: ServerPlayerEntity, moveTime: Int, attackHostiles: Boolean): Boolean =
        user.world.spawnEntity(ClonePlayerEntity(user.world, user, attackHostiles).apply {
            moveVelocity = user.horizontalRotationVector * (user.velocity.length() * 2.0).coerceAtMost(1.0)
            this.moveTime = moveTime
            if (user.velocity.y > 0) {
                jumpControl.setActive()
                setJumping(true)
            }
        })

    companion object {

        // Default Values
        private const val DEFAULT_CLONE_INTERVAL = 10
        private const val DEFAULT_CLONE_MOVE_TIME = 3 * 20
        private const val DEFAULT_INVISIBLE_DURATION = 3 * 20
        private const val DEFAULT_CLONE_COUNT = 1
        private const val DEFAULT_ATTACK_HOSTILES = false

        // Parameter Names
        private const val PARAM_CLONE_INTERVAL = "clone_interval"  // 分身间隔
        private const val PARAM_CLONE_MOVE_TIME = "clone_move_time"  // 分身移动时间
        private const val PARAM_INVISIBLE_DURATION = "invisible_duration"  // 隐身时长
        private const val PARAM_CLONE_COUNT = "clone_count"  // 分身数量
        private const val PARAM_ATTACK_HOSTILES = "attack_hostiles"  // 攻击敌人

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应隐身时长
        private const val ENHANCEMENT_COUNT = "count"  // 对应分身数量
        private const val ENHANCEMENT_ATTACK_HOSTILES = "attack"  // 对应攻击敌人
    }
}