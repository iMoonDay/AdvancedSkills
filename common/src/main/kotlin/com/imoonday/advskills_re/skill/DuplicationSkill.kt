package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.server.network.*

class DuplicationSkill : Skill(
    id = "duplication",
    types = listOf(SkillType.SUMMON),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), SendPlayerVelocityTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(ClonePlayerEntity(user.world, user).apply {
            moveVelocity = user.horizontalRotationVector * (user.velocity.length() * 2.0).coerceAtMost(1.0)
            moveTime = 20 * 3
            if (user.velocity.y > 0) {
                jumpControl.setActive()
                setJumping(true)
            }
        })
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, 20 * 3, 0, true, false, true))
        return UseResult.success()
    }
}