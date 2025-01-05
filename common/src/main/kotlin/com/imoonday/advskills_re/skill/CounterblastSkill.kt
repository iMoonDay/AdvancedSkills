package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class CounterblastSkill : PassiveSkill(
    Settings(
        id = "counterblast",
        rarity = SkillRarity.SUPERB
    )
), PostAttackedTrigger, ProgressTrigger, StopTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_BASE_TRIGGER_CHANCE, DEFAULT_BASE_TRIGGER_CHANCE)
            .addParameter(PARAM_BLAST_SOUND, DEFAULT_BLAST_SOUND)
            .addParameter(
                name = PARAM_BLAST_RANGE,
                baseValue = DEFAULT_BLAST_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_REPEL_FORCE,
                baseValue = DEFAULT_REPEL_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addEnhancement(
                id = ENHANCEMENT_TRIGGER_CHANCE,
                value = 0.04,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
        super.initDefaultSettings(settings)
    }

    override fun isCustomToggles(): Boolean = true

    override fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postAttacked(source, player, attacker)
        if (!isAvailable(player)) return

        if (attacker != null) {
            val data = player.getPersistentData()
            data.putInt(NBT_DAMAGED_TIMES, data.getInt(NBT_DAMAGED_TIMES).coerceAtLeast(0) + 1)
            val times = data.getInt(NBT_DAMAGED_TIMES)
            val probability = getProbability(player, times)
            if (times > 0 && player.random.nextFloat() < probability) {
                data.remove(NBT_DAMAGED_TIMES)
                val range = getDoubleParam(PARAM_BLAST_RANGE, player, DEFAULT_BLAST_RANGE)
                val power = getDoubleParam(PARAM_REPEL_FORCE, player, DEFAULT_REPEL_FORCE)
                player.world.getOtherEntities(
                    player,
                    player.boundingBox.expand(range)
                ) { it is LivingEntity }.forEach {
                    it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(2.0 * power))
                    it.velocityDirty = true
                    (it as? ServerPlayerEntity)?.updateVelocity()
                }
                player.playSoundFromParam(PARAM_BLAST_SOUND, DEFAULT_BLAST_SOUND)
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
        player.getPersistentData().remove(NBT_DAMAGED_TIMES)
    }

    private fun getProbability(player: PlayerEntity, times: Int) =
        (getFloatParam(
            PARAM_BASE_TRIGGER_CHANCE,
            player,
            DEFAULT_BASE_TRIGGER_CHANCE
        ) * times + getAdditionalProbability(player)).coerceIn(0f, 1f)

    private fun getAdditionalProbability(player: PlayerEntity) =
        getFloatParam(ENHANCEMENT_TRIGGER_CHANCE, player, 0f)

    override fun shouldDisplay(player: PlayerEntity): Boolean = player.getPersistentData().contains(NBT_DAMAGED_TIMES)

    override fun getProgress(player: PlayerEntity): Double =
        getProbability(player, player.getPersistentData().getInt(NBT_DAMAGED_TIMES)).toDouble()

    companion object {

        // NBT Keys
        private const val NBT_DAMAGED_TIMES = "DamagedTimes"

        // Default Values
        private const val DEFAULT_BASE_TRIGGER_CHANCE = 0.2f
        private const val DEFAULT_BLAST_RANGE = 5.0
        private const val DEFAULT_REPEL_FORCE = 1.0
        private val DEFAULT_BLAST_SOUND = SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP

        // Parameter Names
        private const val PARAM_BASE_TRIGGER_CHANCE = "base_trigger_chance"  // 基础触发概率
        private const val PARAM_BLAST_SOUND = "blast_sound"  // 爆发音效
        private const val PARAM_BLAST_RANGE = "blast_range"  // 爆发范围
        private const val PARAM_REPEL_FORCE = "repel_force"  // 击退力度

        // Enhancement IDs
        private const val ENHANCEMENT_TRIGGER_CHANCE = "chance"  // 对应触发概率
        private const val ENHANCEMENT_RANGE = "range"  // 对应爆发范围
        private const val ENHANCEMENT_FORCE = "force"  // 对应击退力度
    }
}