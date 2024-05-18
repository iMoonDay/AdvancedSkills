package com.imoonday.skill

import com.imoonday.component.properties
import com.imoonday.trigger.PostDamagedTrigger
import com.imoonday.trigger.ProgressTrigger
import com.imoonday.trigger.TickTrigger
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

class PainFeedbackSkill : PassiveSkill(
    id = "pain_feedback",
    rarity = Rarity.SUPERB,
), PostDamagedTrigger, ProgressTrigger, TickTrigger {

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.properties.containsUuid("lastAttacker")) {
            val lastAttacker = player.properties.getUuid("lastAttacker")
            val attacker = player.serverWorld.getEntity(lastAttacker)
            if (attacker == null || !attacker.isAlive || attacker.isRemoved) {
                player.reset()
            }
        }
    }

    override fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postDamaged(amount, source, player, attacker)
        if (attacker == null) return
        if (!player.properties.containsUuid("lastAttacker")) player.properties.putUuid("lastAttacker", attacker.uuid)
        val lastAttacker = player.properties.getUuid("lastAttacker")
        if (attacker.uuid == lastAttacker) {
            player.properties.putFloat("totalDamaged", player.properties.getFloat("totalDamaged") + amount)
        } else {
            player.properties.putFloat("totalDamaged", amount)
            player.properties.putUuid("lastAttacker", attacker.uuid)
        }
        val damage = player.properties.getFloat("totalDamaged")
        if (damage >= 10f) {
            player.reset()
            attacker.damage(player.damageSources.thorns(player), damage * 0.25f)
        }
    }

    private fun ServerPlayerEntity.reset() {
        properties.remove("totalDamaged")
        properties.remove("lastAttacker")
    }

    override fun shouldDisplay(player: PlayerEntity): Boolean =
        player.properties.containsUuid("lastAttacker") && player.properties.getFloat("totalDamaged") > 0

    override fun getProgress(player: PlayerEntity): Double =
        (player.properties.getFloat("totalDamaged") / 10.0).coerceIn(0.0, 1.0)
}