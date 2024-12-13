package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

private const val REMAINING_EFFECTS = "RemainingEffects"

class DamageAbsorptionSkill : Skill(
    id = "damage_absorption",
    types = listOf(SkillType.DEFENSE, SkillType.RESTORATION),
    cooldown = 60,
    rarity = SkillRarity.LEGENDARY,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.EFFECT_COUNT)
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger {

    init {
        addEnhancementTooltipWithArg(SkillEnhancements.EFFECT_COUNT) { it.level }
    }

    override val persistTime: Int = 15 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putInt(REMAINING_EFFECTS, user.getEnhancementLvl(SkillEnhancements.EFFECT_COUNT))
    })

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing() || amount <= 0) return false
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        player.heal(amount)

        val data = player.getActiveData()
        val remaining = data.getInt(REMAINING_EFFECTS)
        if (remaining <= 0) {
            player.stopAndCooldown()
        } else {
            data.putInt(REMAINING_EFFECTS, remaining - 1)
        }
        return true
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}