package com.imoonday.skill

import com.imoonday.component.properties
import com.imoonday.trigger.PostDamagedTrigger
import com.imoonday.trigger.ProgressTrigger
import com.imoonday.util.playSound
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class CounterblastSkill : PassiveSkill(
    id = "counterblast",
    rarity = Rarity.SUPERB,
), PostDamagedTrigger, ProgressTrigger {

    override fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postDamaged(amount, source, player, attacker)
        if (attacker != null) {
            player.properties.putInt("damagedTimes", player.properties.getInt("damagedTimes") + 1)
            if (player.properties.getInt("damagedTimes") >= 5) {
                player.properties.remove("damagedTimes")
                player.world.getNonSpectatingEntities(
                    LivingEntity::class.java,
                    player.boundingBox.expand(5.0)
                ).filterIsInstance<LivingEntity>()
                    .forEach { it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(2.0)) }
                player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            }
        }
    }

    override fun shouldDisplay(player: PlayerEntity): Boolean = player.properties.contains("damagedTimes")

    override fun getProgress(player: PlayerEntity): Double = player.properties.getInt("damagedTimes") / 5.0
}