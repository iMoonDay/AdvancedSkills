package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class DashSkill : Skill(
    Settings(
        id = "dash",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 2,
        rarity = SkillRarity.UNCOMMON
    )
) {

    init {
        this.settings.addParameter("dash_sound", ModSounds.DASH)

        addParameter(
            name = "velocity_multiplier",
            baseValue = 1.5,
            enhancementId = "multiplier",
            value = 0.1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = user.run {
        stopFallFlying()
        val multiplier = getDoubleParam("velocity_multiplier", user, 1.5)
        velocity = rotationVector.normalize().multiply(multiplier)
        updateVelocity()
        spawnParticles(
            ParticleTypes.CLOUD, false, pos, 10,
            0.5, 0.0, 0.5, 0.1
        )
        UseResult.success(sound = getSoundEventParam("dash_sound", ModSounds.DASH.get()))
    }
}