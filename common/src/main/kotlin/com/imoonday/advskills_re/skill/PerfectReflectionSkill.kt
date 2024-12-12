package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class PerfectReflectionSkill : ReflectionSkill(
    id = "perfect_reflection",
    cooldown = 5,
    rarity = SkillRarity.EPIC,
    duration = 2,
    enhancements = setOf(SkillEnhancements.HEALING_AMOUNT, SkillEnhancements.POWER)
) {

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing()) return false
        val time = getStartTime(player)?.let {
            System.currentTimeMillis() - it
        }
        player.stopUsing()
        player.stopCooling()
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        val heal = getEnhancedValue(player, SkillEnhancements.HEALING_AMOUNT, amount / 10)
        player.heal(heal)
        player.sendMessage(message("success", time?.let { " ${it / 1000.0}s" } ?: ""), true)
        attacker?.run {
            damage(
                player.damageSources.thorns(player),
                getEnhancedValue(player, SkillEnhancements.DAMAGE, amount * 1.5f)
            )
            val power = 1.5 + player.getEnhancementLvl(SkillEnhancements.POWER) * 0.1
            velocity = pos.subtract(player.pos).normalize().multiply(power, 0.0, power).add(0.0, 0.5, 0.0)
            velocityDirty = true
            (this as? ServerPlayerEntity)?.updateVelocity()
        }
        return true
    }
}