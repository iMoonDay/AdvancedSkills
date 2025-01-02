package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.projectile.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

//TODO 不要对使用者有碰撞
class FireballSkill : Skill(
    Settings(
        id = "fireball",
        types = listOf(SkillType.DESTRUCTION),
        cooldown = 8,
        rarity = SkillRarity.RARE
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("shoot_sound", SoundEvents.ENTITY_ENDER_DRAGON_SHOOT)
            .addParameter(
                name = "power",
                baseValue = 1,
                enhancementId = "power",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "launch_count",
                baseValue = 1,
                enhancementId = "count",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "velocity_multiplier",
                baseValue = 1.5,
                enhancementId = "multiplier",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val power = getIntParam("power", this, 1)
            val launchCount = getIntParam("launch_count", this, 0)
            val velocityMultiplier = getDoubleParam("velocity_multiplier", this, 1.5)
            val shootSound = getSoundEventParam("shoot_sound", SoundEvents.ENTITY_ENDER_DRAGON_SHOOT)
            executeAndAddTask(5, launchCount) { spawnFireball(power, velocityMultiplier, shootSound) }
        }
        return UseResult.success()
    }

    private fun ServerPlayerEntity.spawnFireball(
        power: Int,
        velocityMultiplier: Double,
        shootSound: SoundEvent?
    ): Boolean {
        val rotation = rotationVector.normalize().multiply(velocityMultiplier)
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
                shootSound?.let(::playSound)
            }
        )
    }
}