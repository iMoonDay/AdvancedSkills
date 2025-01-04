package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class ResuscitationSkill : Skill(
    Settings(
        id = "resuscitation",
        types = listOf(SkillType.PASSIVE, SkillType.DEFENSE),
        cooldown = 300,
        rarity = SkillRarity.LEGENDARY
    )
), DeathTrigger, AutoStopTrigger, DamageTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_REVIVE_HEALTH, DEFAULT_REVIVE_HEALTH)
            .addParameter(
                name = PARAM_IMMUNE_DURATION,
                baseValue = DEFAULT_IMMUNE_DURATION,
                enhancementId = ENHANCEMENT_IMMUNE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_REGEN_DURATION,
                baseValue = DEFAULT_REGEN_DURATION,
                enhancementId = ENHANCEMENT_REGEN,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (player.isCooling()) return true
        player.health = getFloatParam(PARAM_REVIVE_HEALTH, player, DEFAULT_REVIVE_HEALTH, 0.0f)
        player.startUsing()
        player.startCooling()
        player.world.sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING)
        val duration = getIntParam(PARAM_REGEN_DURATION, player, DEFAULT_REGEN_DURATION)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, duration))
        return false
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.isUsing()

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_IMMUNE_DURATION, player, DEFAULT_IMMUNE_DURATION, 0)

    companion object {
        // Default Values
        private const val DEFAULT_REVIVE_HEALTH = 1.0f
        private const val DEFAULT_IMMUNE_DURATION = 2 * 20
        private const val DEFAULT_REGEN_DURATION = 30 * 20

        // Parameter Names
        private const val PARAM_REVIVE_HEALTH = "revive_health"  // 复活生命值
        private const val PARAM_IMMUNE_DURATION = "immune_duration"  // 免疫持续时间
        private const val PARAM_REGEN_DURATION = "regen_duration"  // 恢复持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_IMMUNE = "immune"  // 对应免疫时间
        private const val ENHANCEMENT_REGEN = "regen"  // 对应恢复时间
    }
}
