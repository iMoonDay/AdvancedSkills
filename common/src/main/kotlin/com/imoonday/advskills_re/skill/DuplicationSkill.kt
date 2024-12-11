package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.server.network.*

class DuplicationSkill : Skill(
    id = "duplication",
    types = listOf(SkillType.SUMMON),
    cooldown = 30,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.STATUS_EFFECT_DURATION, SkillEnhancements.SUMMON_AMOUNT)
), SendPlayerVelocityTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.executeAndAddTask(10, user.getEnhancementLvl(SkillEnhancements.SUMMON_AMOUNT)) { summonClones(user) }
        val duration = getEnhancedValue(user, SkillEnhancements.STATUS_EFFECT_DURATION, 3 * 20)
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, true, false, true))
        return UseResult.success()
    }

    private fun summonClones(user: ServerPlayerEntity): Boolean {
        return user.world.spawnEntity(ClonePlayerEntity(user.world, user).apply {
            moveVelocity = user.horizontalRotationVector * (user.velocity.length() * 2.0).coerceAtMost(1.0)
            moveTime = 3 * 20
            if (user.velocity.y > 0) {
                jumpControl.setActive()
                setJumping(true)
            }
        })
    }
}