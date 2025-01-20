package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class HorizontalDashSkill : Skill(
    Settings(
        id = "horizontal_dash",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 1,
        rarity = SkillRarity.COMMON
    )
) {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_DASH_SOUND, DEFAULT_DASH_SOUND)
            .addParameter(
                name = PARAM_DASH_FORCE,
                baseValue = DEFAULT_DASH_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            val dashForce = getDoubleParam(PARAM_DASH_FORCE, this, DEFAULT_DASH_FORCE)
            velocity = horizontalRotationVector.normalize().multiply(dashForce).withAxis(Direction.Axis.Y, velocity.y)
            updateVelocity()
            spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success(sound = getSoundEventParam(PARAM_DASH_SOUND, DEFAULT_DASH_SOUND.get()))
    }

    companion object {

        // Default Values
        private const val DEFAULT_DASH_FORCE = 1.5
        private val DEFAULT_DASH_SOUND = ModSounds.DASH

        // Parameter Names
        private const val PARAM_DASH_SOUND = "dash_sound"  // 冲刺音效
        private const val PARAM_DASH_FORCE = "dash_force"  // 冲刺力度

        // Enhancement IDs
        private const val ENHANCEMENT_FORCE = "force"  // 对应冲刺力度
    }
}