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
    id = "negative_resistance",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.EFFECT_COUNT)
), AutoStopTrigger, StatusEffectTrigger, UsingRenderTrigger {

    init {
        addEnhanceableParameter(
            timeParamName,
            5 * 20,
            "time",
            0.2f,
            Enhancement.Operation.MULTIPLY,
            5
        ) { (it * 100).toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(REMAINING_EFFECTS, user.getEnhancementLvl(SkillEnhancements.EFFECT_COUNT))
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

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}