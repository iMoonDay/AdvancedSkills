package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ExtremeEvasionSkill : Skill(
    Settings(
        id = "extreme_evasion",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 10,
        rarity = SkillRarity.EPIC
    )
), AutoStopTrigger, DamageTrigger, SendPlayerVelocityTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("moving_sound", ModSounds.DASH)
            .addParameter(
                name = timeParamName,
                baseValue = 10,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "velocity_multiplier",
                baseValue = 2.0,
                enhancementId = "velocity",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this) {
        user.run {
            stopFallFlying()
            val multiplier = getDoubleParam("velocity_multiplier", user, 2.0)
            velocity = (if (velocity.x == 0.0 && velocity.z == 0.0) rotationVector else velocity)
                .normalize()
                .multiply(multiplier, 0.0, multiplier)
            updateVelocity()

            serverWorld.spawnParticles(
                ParticleTypes.CLOUD,
                x, y, z, 10,
                -velocity.x, 0.0, -velocity.z, 0.0
            )
        }
    }.withSound(getSoundEventParam("moving_sound", ModSounds.DASH.get()))

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.isUsing()

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}