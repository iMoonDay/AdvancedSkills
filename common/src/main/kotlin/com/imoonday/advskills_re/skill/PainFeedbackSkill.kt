package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class PainFeedbackSkill : PassiveSkill(
    id = "pain_feedback",
    cooldown = 3,
    rarity = Rarity.SUPERB,
), PostDamagedTrigger {

    override fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postDamaged(amount, source, player, attacker)
        if (attacker == null || player.isCooling()) return
        if (amount > 0f && player.random.nextFloat() < 0.25f) {
            attacker.damage(player.damageSources.thorns(player), amount * 0.25f)
            player.startCooling()
        }
    }
}