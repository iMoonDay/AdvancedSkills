package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class InstantExplosiveSkill : Skill(
    id = "instant_explosive",
    types = listOf(SkillType.DESTRUCTION),
    cooldown = 20,
    rarity = SkillRarity.EPIC,
    sound = ModSounds.FIRE,
    enhancements = setOf(SkillEnhancements.LAUNCH_COUNT)
), SendPlayerVelocityTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val times = getEnhancementLvl(SkillEnhancements.LAUNCH_COUNT)
            executeAndAddTask(5, times) { spawnTnt() }
        }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnTnt(): Boolean {
        val rotation = velocity.add(rotationVector.normalize().multiply(1.5))
        return world.spawnEntity(
            UnstableTntEntity(
                world,
                x + rotation.x,
                eyeY,
                z + rotation.z,
                this,
                rotation
            )
        )
    }
}