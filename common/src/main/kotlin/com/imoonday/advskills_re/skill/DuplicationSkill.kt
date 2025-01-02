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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("summon_interval", 10)
            .addParameter("movement_time", 3 * 20)
            .addParameter(
                name = "invisible_duration",
                baseValue = 3 * 20,
                enhancementId = "duration",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "summon_amount",
                baseValue = 1,
                enhancementId = "amount",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val interval = getIntParam("summon_interval", user, 10, 0)
        val amount = getIntParam("summon_amount", user, 1)
        val time = getIntParam("movement_time", user, 3 * 20)
        user.executeAndAddTask(interval, amount) { summonClones(user, time) }
        val duration = getIntParam("invisible_duration", user, 3 * 20)
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, true, false, true))
        return UseResult.success()
    }

    private fun summonClones(user: ServerPlayerEntity, moveTime: Int): Boolean {
        return user.world.spawnEntity(ClonePlayerEntity(user.world, user).apply {
            moveVelocity = user.horizontalRotationVector * (user.velocity.length() * 2.0).coerceAtMost(1.0)
            this.moveTime = moveTime
            if (user.velocity.y > 0) {
                jumpControl.setActive()
                setJumping(true)
            }
        })
    }
}