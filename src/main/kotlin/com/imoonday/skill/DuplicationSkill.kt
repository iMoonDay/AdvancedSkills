package com.imoonday.skill

import com.imoonday.entity.ClonePlayerEntity
import com.imoonday.trigger.SendPlayerVelocityTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.horizontalRotationVector
import com.imoonday.util.times
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity

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
            setJumping(user.velocity.y > 0)
        })
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, 20))
        return UseResult.success()
    }
}