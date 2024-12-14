package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.projectile.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

//TODO 不要对使用者有碰撞
class FireballSkill : Skill(
    id = "fireball",
    types = listOf(SkillType.DESTRUCTION),
    cooldown = 8,
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.LAUNCH_COUNT, SkillEnhancements.POWER, SkillEnhancements.VELOCITY)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val power = 1 + user.getEnhancementLvl(SkillEnhancements.POWER)
            val launchCount = user.getEnhancementLvl(SkillEnhancements.LAUNCH_COUNT)
            val velocity = 1.5 + user.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.1
            executeAndAddTask(5, launchCount) { spawnFireball(power, velocity) }
        }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnFireball(power: Int, velocity: Double): Boolean {
        val rotation = rotationVector.normalize().multiply(velocity)
        return world.spawnEntity(
            FireballEntity(
                world,
                this,
                rotation.x,
                rotation.y,
                rotation.z,
                power
            ).apply {
                setPosition(x + rotation.x, eyeY, z + rotation.z)
            }.also {
                playSound(SoundEvents.ENTITY_ENDER_DRAGON_SHOOT)
            }
        )
    }
}