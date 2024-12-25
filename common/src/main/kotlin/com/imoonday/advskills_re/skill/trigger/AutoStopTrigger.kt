package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger, UnequipTrigger {

    val persistTime: Int

    fun getModifiedPersistTime(player: PlayerEntity): Int =
        applyPersistTimeEnhancements(player, getPersistTimeOrDefault(getAsSkill(), persistTime))

    fun applyPersistTimeEnhancements(player: PlayerEntity, time: Int): Int {
        val enhancements = getAsSkill().availableEnhancements
        return when {
            SkillEnhancements.PERSISTENT_TIME in enhancements -> {
                applyTimeBoostEnhancement(player, time)
            }

            SkillEnhancements.CHARGE_TIME in enhancements -> {
                applyChargeTimeReductionEnhancement(player, time)
            }

            else -> time
        }
    }

    fun applyTimeBoostEnhancement(player: PlayerEntity, time: Int): Int =
        getEnhancedValue(player, SkillEnhancements.PERSISTENT_TIME, time)

    fun applyChargeTimeReductionEnhancement(player: PlayerEntity, time: Int): Int =
        getEnhancedValue(player, SkillEnhancements.CHARGE_TIME, time).coerceAtLeast(0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (canAutoStop(player) && usedTime >= getModifiedPersistTime(player)) {
            onStop(player)
            player.stopUsing()
        }
    }

    fun onStop(player: ServerPlayerEntity) = Unit

    fun canAutoStop(player: PlayerEntity): Boolean = true

    override fun getProgress(player: PlayerEntity): Double {
        val time = getModifiedPersistTime(player)
        return (time - player.getUsedTime()) / time.toDouble()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        onStop(player)
    }

    companion object {

        fun getPersistTimeOrDefault(skill: Skill, time: Int) = (SkillConfig.get().getModifier(skill.id)?.time
            ?: GlobalConfig.get().skillConfig.getModifier(skill.id)?.time
            ?: time)
    }
}