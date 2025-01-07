package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class TripleJumpSkill : Skill(
    Settings(
        id = "triple_jump",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 5,
        rarity = SkillRarity.RARE
    )
) {

    init {
        settings.addParameter(
            name = PARAM_JUMP_FORCE,
            baseValue = DEFAULT_JUMP_FORCE,
            enhancementId = ENHANCEMENT_FORCE,
            value = 0.1,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            fallDistance = 0.0f
            jump()
            val jumpForce = getDoubleParam(PARAM_JUMP_FORCE, user, DEFAULT_JUMP_FORCE)
            velocity = velocity.multiply(1.0, jumpForce, 1.0)
            updateVelocity()
            spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success()
    }

    companion object {

        // Default Values
        private const val DEFAULT_JUMP_FORCE = 1.7

        // Parameter Names
        private const val PARAM_JUMP_FORCE = "jump_force"  // 跳跃力度

        // Enhancement IDs
        private const val ENHANCEMENT_FORCE = "force"  // 对应力度
    }
}