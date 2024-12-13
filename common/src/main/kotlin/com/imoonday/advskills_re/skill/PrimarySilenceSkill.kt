package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class PrimarySilenceSkill : Skill(
    id = "primary_silence",
    types = listOf(SkillType.CONTROL),
    cooldown = 20,
    rarity = SkillRarity.SUPERB,
    sound = ModSounds.FIRE,
    enhancements = setOf(SkillEnhancements.RANGE, SkillEnhancements.LAUNCH_COUNT)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val extraRange = user.getEnhancementLvl(SkillEnhancements.RANGE)
        val times = user.getEnhancementLvl(SkillEnhancements.LAUNCH_COUNT)
        user.executeAndAddTask(5, times) { user.spawnEnergyBall(extraRange) }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnEnergyBall(extraRange: Int): Boolean {
        val rotation = rotationVector.normalize().multiply(1.5)
        return world.spawnEntity(
            SilenceEnergyBallEntity(
                this,
                rotationVector.x,
                rotationVector.y,
                rotationVector.z,
                world
            ).apply {
                setPosition(x + rotation.x, eyeY, z + rotation.z)
                range += extraRange
            }
        )
    }
}