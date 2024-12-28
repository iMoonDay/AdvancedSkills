package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

private const val DAMAGED_TIMES_KEY = "damagedTimes"

class CounterblastSkill : PassiveSkill(
    Settings(
        id = "counterblast",
        rarity = SkillRarity.SUPERB
    ), customToggles = true
//    enhancements = setOf(SkillEnhancements.RANGE, SkillEnhancements.POWER, SkillEnhancements.CHANCE),
), PostAttackedTrigger, ProgressTrigger, StopTrigger {

    init {
        addEnhancementTooltipWithArg(SkillEnhancements.RANGE) { it.level }
        addEnhancementTooltipWithArg(SkillEnhancements.POWER) { it.level * 10 }
        addEnhancementTooltipWithArg(SkillEnhancements.CHANCE) { it.level * 4 }
    }

    override fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postAttacked(source, player, attacker)
        if (!player.isAvailable()) return

        if (attacker != null) {
            val data = player.getPersistentData()
            data.putInt(DAMAGED_TIMES_KEY, data.getInt(DAMAGED_TIMES_KEY).coerceAtLeast(0) + 1)
            val times = data.getInt(DAMAGED_TIMES_KEY)
            val chance = getAdditionalChance(player)
            if (times > 0 && player.random.nextFloat() < 0.2f * times + chance) {
                data.remove(DAMAGED_TIMES_KEY)
                val range = player.getEnhancementLvl(SkillEnhancements.RANGE)
                val power = 1.0 + player.getEnhancementLvl(SkillEnhancements.POWER) * 0.1
                player.world.getOtherEntities(
                    player,
                    player.boundingBox.expand(5.0 + range)
                ) { it is LivingEntity }.forEach {
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

    override fun postStop(player: PlayerEntity) {
        super.postStop(player)
        player.getPersistentData().remove(DAMAGED_TIMES_KEY)
    }

    private fun getAdditionalChance(player: PlayerEntity) =
        player.getEnhancementLvl(SkillEnhancements.CHANCE) * 0.04f

    override fun shouldDisplay(player: PlayerEntity): Boolean = player.getPersistentData().contains(DAMAGED_TIMES_KEY)

    override fun getProgress(player: PlayerEntity): Double =
        player.getPersistentData().getInt(DAMAGED_TIMES_KEY) / ((1.0 - getAdditionalChance(player)) * 5.0)
}