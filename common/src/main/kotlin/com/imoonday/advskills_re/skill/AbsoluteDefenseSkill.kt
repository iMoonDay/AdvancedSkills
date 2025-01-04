package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class AbsoluteDefenseSkill : Skill(
    Settings(
        id = "absolute_defense",
        types = listOf(SkillType.DEFENSE),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_BLOCK_SOUND, SoundEvents.ITEM_SHIELD_BLOCK)
            .addParameter(PARAM_BREAK_SOUND, SoundEvents.ITEM_SHIELD_BREAK)
            .addParameter(
                name = PARAM_DURATION,
                baseValue = DEFAULT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DEFENSE_COUNT,
                baseValue = DEFAULT_DEFENSE_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 4,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(NBT_REMAINING_COUNT, getIntParam(PARAM_DEFENSE_COUNT, user, DEFAULT_DEFENSE_COUNT))
    })

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_DURATION, player, DEFAULT_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing() || amount <= 0) return false

        val data = player.getActiveData()
        val remaining = data.getInt(NBT_REMAINING_COUNT)

        return when {
            remaining <= 0 -> handleNoRemainingDefense(player)
            else -> handleRemainingDefense(player, data, remaining)
        }
    }

    private fun handleNoRemainingDefense(player: ServerPlayerEntity): Boolean {
        val sound = if (player.hasEnhancement(ENHANCEMENT_COUNT)) {
            PARAM_BREAK_SOUND
        } else {
            PARAM_BLOCK_SOUND
        }
        player.playSoundFromParam(sound, SoundEvents.ITEM_SHIELD_BLOCK)
        player.stopAndCooldown()
        return true
    }

    private fun handleRemainingDefense(
        player: ServerPlayerEntity,
        data: NbtCompound,
        remaining: Int
    ): Boolean {
        player.playSoundFromParam(PARAM_BLOCK_SOUND, SoundEvents.ITEM_SHIELD_BLOCK)
        data.putInt(NBT_REMAINING_COUNT, remaining - 1)
        return true
    }

    companion object {

        private const val DEFAULT_DURATION = 30 * 20
        private const val DEFAULT_DEFENSE_COUNT = 1

        // NBT Keys
        private const val NBT_REMAINING_COUNT = "RemainingEffects"

        // Parameter Names
        private const val PARAM_BLOCK_SOUND = "block_sound"
        private const val PARAM_BREAK_SOUND = "break_sound"
        private const val PARAM_DURATION = "persist_time"
        private const val PARAM_DEFENSE_COUNT = "defense_count"

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"
        private const val ENHANCEMENT_COUNT = "count"
    }
}