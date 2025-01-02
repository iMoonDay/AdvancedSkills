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

private const val DAMAGED_TIMES_KEY = "DamagedTimes"

class CounterblastSkill : PassiveSkill(
    Settings(
        id = "counterblast",
        rarity = SkillRarity.SUPERB
    ), customToggles = true
), PostAttackedTrigger, ProgressTrigger, StopTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("basic_probability_each_time", 0.2f)
            .addParameter("blast_sound", SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .addParameter(
                name = "range",
                baseValue = 5.0,
                enhancementId = "range",
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = "power",
                baseValue = 1.0,
                enhancementId = "power",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addEnhancement(
                id = "additional_probability",
                value = 0.04,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
        super.initDefaultSettings(settings)
    }

    override fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postAttacked(source, player, attacker)
        if (!player.isAvailable()) return

        if (attacker != null) {
            val data = player.getPersistentData()
            data.putInt(DAMAGED_TIMES_KEY, data.getInt(DAMAGED_TIMES_KEY).coerceAtLeast(0) + 1)
            val times = data.getInt(DAMAGED_TIMES_KEY)
            val probability = getProbability(player, times)
            if (times > 0 && player.random.nextFloat() < probability) {
                data.remove(DAMAGED_TIMES_KEY)
                val range = getDoubleParam("range", player, 5.0)
                val power = getDoubleParam("power", player, 1.0)
                player.world.getOtherEntities(
                    player,
                    player.boundingBox.expand(range)
                ) { it is LivingEntity }.forEach {
                    it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(2.0 * power))
                    it.velocityDirty = true
                    (it as? ServerPlayerEntity)?.updateVelocity()
                }
                player.playSoundFromParam("blast_sound", SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
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

    private fun getProbability(player: PlayerEntity, times: Int) =
        (getFloatParam(
            "basic_probability_each_time",
            player,
            0.2f
        ) * times + getAdditionalProbability(player)).coerceIn(0f, 1f)

    private fun getAdditionalProbability(player: PlayerEntity) =
        getFloatParam("additional_probability", player, 0f)

    override fun shouldDisplay(player: PlayerEntity): Boolean = player.getPersistentData().contains(DAMAGED_TIMES_KEY)

    override fun getProgress(player: PlayerEntity): Double =
        getProbability(player, player.getPersistentData().getInt(DAMAGED_TIMES_KEY)).toDouble()
}