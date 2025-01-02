package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class PainFeedbackSkill : PassiveSkill(
    Settings(
        id = "pain_feedback",
        cooldown = 5,
        rarity = SkillRarity.SUPERB
    ), customToggles = true
), PostDamagedTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "damage_multiplier",
            baseValue = 0.5f,
            enhancementId = "damage",
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
        super.initDefaultSettings(settings)
    }

    override fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postDamaged(amount, source, player, attacker)
        if (!player.isAvailable()) return

        if (attacker == null || player.isCooling()) return
        if (amount > 0f) {
            val multiplier = getFloatParam("damage_multiplier", player, 0.5f)
            val damage = amount * multiplier
            attacker.damage(player.damageSources.thorns(player), damage)
            player.startCooling()
        }
    }
}