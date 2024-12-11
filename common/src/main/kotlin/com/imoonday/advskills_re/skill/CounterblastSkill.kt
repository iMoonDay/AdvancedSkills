package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
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
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.RANGE, SkillEnhancements.POWER, SkillEnhancements.CHANCE),
), PostAttackedTrigger, ProgressTrigger {

    override fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postAttacked(source, player, attacker)
        if (attacker != null) {
            val data = player.getPersistentData()
            data.putInt("damagedTimes", data.getInt("damagedTimes").coerceAtLeast(0) + 1)
            val times = data.getInt("damagedTimes")
            val chance = getAdditionalChance(player)
            if (times > 0 && player.random.nextFloat() < 0.2f * times + chance) {
                data.remove("damagedTimes")
                val range = player.getEnhancementLvl(SkillEnhancements.RANGE)
                val power = player.getEnhancementLvl(SkillEnhancements.POWER) * 0.2
                player.world.getNonSpectatingEntities(
                    LivingEntity::class.java,
                    player.boundingBox.expand(5.0 + range)
                ).filterNot { it === player }
                    .forEach {
                        it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(2.0 * power))
                        it.velocityDirty = true
                        (it as? ServerPlayerEntity)?.updateVelocity()
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

    private fun getAdditionalChance(player: PlayerEntity) =
        player.getEnhancementLvl(SkillEnhancements.CHANCE) * 0.04f

    override fun shouldDisplay(player: PlayerEntity): Boolean = player.getPersistentData().contains("damagedTimes")

    override fun getProgress(player: PlayerEntity): Double =
        player.getPersistentData().getInt("damagedTimes") / ((1.0 - getAdditionalChance(player)) * 5.0)
}