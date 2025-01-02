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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("dash_sound", ModSounds.DASH)
            .addParameter(
                name = "dash_velocity",
                baseValue = 1.5,
                enhancementId = "velocity",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            val dashVelocity = getDoubleParam("dash_velocity", this, 1.5)
            velocity = rotationVector.withAxis(Direction.Axis.Y, velocity.y).normalize().multiply(dashVelocity)
            updateVelocity()
            spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success(sound = getSoundEventParam("dash_sound", ModSounds.DASH.get()))
    }
}