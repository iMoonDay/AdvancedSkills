package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class NegativeResistanceSkill : Skill(
    Settings(
        id = "negative_resistance",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
), AutoStopTrigger, StatusEffectTrigger, UsingRenderTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = PARAM_RESIST_DURATION,
                baseValue = DEFAULT_RESIST_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_RESIST_COUNT,
                baseValue = DEFAULT_RESIST_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(NBT_REMAINING_EFFECTS, getIntParam(PARAM_RESIST_COUNT, user, DEFAULT_RESIST_COUNT))
    })

    override fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean =
        if (player.isUsing() && !effect.effectType.isBeneficial) {
            (player as? ServerPlayerEntity)?.let {
                it.playSound(ModSounds.PURIFY.get())
                it.spawnParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    false, it.centerPos, 10,
                    0.5, 0.5, 0.5, 0.1
                )

                val data = it.getActiveData()
                val remaining = data.getInt(NBT_REMAINING_EFFECTS)
                if (remaining <= 0) {
                    it.stopAndCooldown()
                } else {
                    data.putInt(NBT_REMAINING_EFFECTS, remaining - 1)
                }
            }
            true
        } else false

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_RESIST_DURATION, player, DEFAULT_RESIST_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {

        // NBT Keys
        private const val NBT_REMAINING_EFFECTS = "RemainingEffects"  // 剩余抵抗次数

        // Default Values
        private const val DEFAULT_RESIST_DURATION = 5 * 20
        private const val DEFAULT_RESIST_COUNT = 1

        // Parameter Names
        private const val PARAM_RESIST_DURATION = "resist_duration"  // 抵抗持续时间
        private const val PARAM_RESIST_COUNT = "resist_count"  // 抵抗次数

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_COUNT = "count"  // 对应抵抗次数
    }
}