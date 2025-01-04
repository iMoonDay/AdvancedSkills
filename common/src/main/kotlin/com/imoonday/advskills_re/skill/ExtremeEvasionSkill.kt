package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
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
            .addParameter(PARAM_EVASION_SOUND, DEFAULT_EVASION_SOUND)
            .addParameter(
                name = PARAM_EVASION_DURATION,
                baseValue = DEFAULT_EVASION_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_EVASION_FORCE,
                baseValue = DEFAULT_EVASION_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this) {
        user.run {
            stopFallFlying()
            val multiplier = getDoubleParam(PARAM_EVASION_FORCE, user, DEFAULT_EVASION_FORCE)
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
    }.withSound(getSoundEventParam(PARAM_EVASION_SOUND, DEFAULT_EVASION_SOUND.get()))

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.isUsing()

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_EVASION_DURATION, player, DEFAULT_EVASION_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {
        // Default Values
        private const val DEFAULT_EVASION_DURATION = 10
        private const val DEFAULT_EVASION_FORCE = 2.0
        private val DEFAULT_EVASION_SOUND = ModSounds.DASH

        // Parameter Names
        private const val PARAM_EVASION_SOUND = "evasion_sound"  // 闪避音效
        private const val PARAM_EVASION_DURATION = "evasion_duration"  // 闪避时长
        private const val PARAM_EVASION_FORCE = "evasion_force"  // 闪避力度

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应闪避时长
        private const val ENHANCEMENT_FORCE = "force"  // 对应闪避力度
    }
}