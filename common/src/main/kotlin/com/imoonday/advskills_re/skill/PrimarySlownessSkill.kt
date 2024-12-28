package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class PrimarySlownessSkill : Skill(
    id = "primary_slowness",
    types = listOf(SkillType.CONTROL),
    cooldown = 6,
    rarity = SkillRarity.RARE,
    sound = ModSounds.FIRE,
    enhancements = setOf(SkillEnhancements.RANGE, SkillEnhancements.LAUNCH_COUNT, SkillEnhancements.SELF_IMMUNE)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val extraRange = user.getEnhancementLvl(SkillEnhancements.RANGE)
        val times = user.getEnhancementLvl(SkillEnhancements.LAUNCH_COUNT)
        val ignoreSelf = user.hasEnhancement(SkillEnhancements.SELF_IMMUNE)
        user.executeAndAddTask(5, times) { user.spawnEnergyBall(extraRange, ignoreSelf) }
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
        )
    }
}