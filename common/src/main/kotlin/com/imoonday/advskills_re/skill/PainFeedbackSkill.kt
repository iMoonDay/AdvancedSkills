package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class PainFeedbackSkill : PassiveSkill(
    id = "pain_feedback",
    cooldown = 5,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.DAMAGE)
), PostDamagedTrigger {

    override fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postDamaged(amount, source, player, attacker)
        if (attacker == null || player.isCooling()) return
        if (amount > 0f) {
            val damage = getEnhancedValue(player, SkillEnhancements.DAMAGE, amount * 0.5f)
            attacker.damage(player.damageSources.thorns(player), damage)
            player.startCooling()
        }
    }
}