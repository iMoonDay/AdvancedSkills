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
            .addParameter(PARAM_SHOOT_SOUND, DEFAULT_SHOOT_SOUND)
            .addParameter(
                name = PARAM_FIREBALL_POWER,
                baseValue = DEFAULT_FIREBALL_POWER,
                enhancementId = ENHANCEMENT_POWER,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_FIREBALL_COUNT,
                baseValue = DEFAULT_FIREBALL_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_LAUNCH_FORCE,
                baseValue = DEFAULT_LAUNCH_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val power = getIntParam(PARAM_FIREBALL_POWER, this, DEFAULT_FIREBALL_POWER)
            val launchCount = getIntParam(PARAM_FIREBALL_COUNT, this, DEFAULT_FIREBALL_COUNT)
            val velocityMultiplier = getDoubleParam(PARAM_LAUNCH_FORCE, this, DEFAULT_LAUNCH_FORCE)
            val shootSound = getSoundEventParam(PARAM_SHOOT_SOUND, DEFAULT_SHOOT_SOUND)
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

    companion object {
        // Default Values
        private const val DEFAULT_FIREBALL_POWER = 1
        private const val DEFAULT_FIREBALL_COUNT = 1
        private const val DEFAULT_LAUNCH_FORCE = 1.5
        private val DEFAULT_SHOOT_SOUND = SoundEvents.ENTITY_ENDER_DRAGON_SHOOT

        // Parameter Names
        private const val PARAM_SHOOT_SOUND = "shoot_sound"  // 发射音效
        private const val PARAM_FIREBALL_POWER = "fireball_power"  // 火球威力
        private const val PARAM_FIREBALL_COUNT = "fireball_count"  // 火球数量
        private const val PARAM_LAUNCH_FORCE = "launch_force"  // 发射力度

        // Enhancement IDs
        private const val ENHANCEMENT_POWER = "power"  // 对应火球威力
        private const val ENHANCEMENT_COUNT = "count"  // 对应火球数量
        private const val ENHANCEMENT_FORCE = "force"  // 对应发射力度
    }
}