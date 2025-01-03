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

private const val REMAINING_EFFECTS = "RemainingEffects"

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
                name = "persist_time",
                baseValue = 5 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "effect_count",
                baseValue = 1,
                enhancementId = "count",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(REMAINING_EFFECTS, getIntParam("effect_count", user, 1))
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
                val remaining = data.getInt(REMAINING_EFFECTS)
                if (remaining <= 0) {
                    it.stopAndCooldown()
                } else {
                    data.putInt(REMAINING_EFFECTS, remaining - 1)
                }
            }
            true
        } else false

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("persist_time", player, 5 * 20, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}