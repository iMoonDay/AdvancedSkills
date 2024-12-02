package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class CounterblastSkill : PassiveSkill(
    id = "counterblast",
    rarity = Rarity.SUPERB,
), PostAttackedTrigger, ProgressTrigger {

    override fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postAttacked(source, player, attacker)
        if (attacker != null) {
            val data = player.getPersistentData()
            data.putInt("damagedTimes", data.getInt("damagedTimes").coerceAtLeast(0) + 1)
            val times = data.getInt("damagedTimes")
            if (times > 0 && player.random.nextFloat() < 0.2f * times) {
                data.remove("damagedTimes")
                player.world.getNonSpectatingEntities(
                    LivingEntity::class.java,
                    player.boundingBox.expand(5.0)
                ).filterNot { it === player }
                    .forEach {
                        it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(2.0))
                        it.velocityDirty = true
                        (it as? ServerPlayerEntity)?.sendPacket(EntityVelocityUpdateS2CPacket(it))
                    }
                player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
                player.spawnParticles(
                    ParticleTypes.CLOUD,
                    false, player.pos, 250,
                    5.0, 1.0, 5.0, 0.1
                )
            }
        }
    }

    override fun shouldDisplay(player: PlayerEntity): Boolean = player.getPersistentData().contains("damagedTimes")

    override fun getProgress(player: PlayerEntity): Double = player.getPersistentData().getInt("damagedTimes") / 5.0
}