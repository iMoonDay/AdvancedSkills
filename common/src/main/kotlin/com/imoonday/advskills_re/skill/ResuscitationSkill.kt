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
            .addParameter("revive_health", 1.0f)
            .addParameter(
                name = "persist_time",
                baseValue = 2 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "regeneration_duration",
                baseValue = 30 * 20,
                enhancementId = "duration",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (player.isCooling()) return true
        player.health = getFloatParam("revive_health", player, 1.0f, 0.0f)
        player.startUsing()
        player.startCooling()
        player.world.sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING)
        val duration = getIntParam("regeneration_duration", player, 30 * 20)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, duration))
        return false
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.isUsing()

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("persist_time", player, 2 * 20, 0)
}
