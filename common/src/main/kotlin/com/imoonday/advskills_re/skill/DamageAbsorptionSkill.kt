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

class DamageAbsorptionSkill : Skill(
    Settings(
        id = "damage_absorption",
        types = listOf(SkillType.DEFENSE, SkillType.RESTORATION),
        cooldown = 60,
        rarity = SkillRarity.LEGENDARY
    )
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_ABSORB_SOUND, DEFAULT_ABSORB_SOUND)
            .addParameter(PARAM_BREAK_SOUND, DEFAULT_BREAK_SOUND)
            .addParameter(
                name = PARAM_DURATION,
                baseValue = DEFAULT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_ABSORB_COUNT,
                baseValue = DEFAULT_ABSORB_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 4,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(NBT_REMAINING_COUNT, getIntParam(PARAM_ABSORB_COUNT, user, DEFAULT_ABSORB_COUNT))
    })

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing() || amount <= 0) return false
        player.heal(amount)

        val data = player.getActiveData()
        val remaining = data.getInt(NBT_REMAINING_COUNT)
        if (remaining <= 0) {
            if (player.hasEnhancement(ENHANCEMENT_COUNT)) {
                player.playSoundFromParam(PARAM_BREAK_SOUND, DEFAULT_BREAK_SOUND)
            } else {
                player.playSoundFromParam(PARAM_ABSORB_SOUND, DEFAULT_ABSORB_SOUND)
            }
            player.stopAndCooldown()
        } else {
            player.playSoundFromParam(PARAM_ABSORB_SOUND, DEFAULT_ABSORB_SOUND)
            data.putInt(NBT_REMAINING_COUNT, remaining - 1)
        }
        return true
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_DURATION, player, DEFAULT_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {
        // NBT Keys
        private const val NBT_REMAINING_COUNT = "RemainingCount"

        // Default Values
        private const val DEFAULT_DURATION = 30 * 20
        private const val DEFAULT_ABSORB_COUNT = 1
        private val DEFAULT_ABSORB_SOUND = SoundEvents.ITEM_SHIELD_BLOCK
        private val DEFAULT_BREAK_SOUND = SoundEvents.ITEM_SHIELD_BREAK

        // Parameter Names
        private const val PARAM_ABSORB_SOUND = "absorb_sound"  // 吸收音效
        private const val PARAM_BREAK_SOUND = "break_sound"  // 破碎音效
        private const val PARAM_DURATION = "duration"  // 持续时间
        private const val PARAM_ABSORB_COUNT = "absorb_count"  // 吸收次数

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_COUNT = "count"  // 对应吸收次数
    }
}