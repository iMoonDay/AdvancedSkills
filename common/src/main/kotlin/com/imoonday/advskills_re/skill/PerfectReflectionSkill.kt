package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.playSound
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class PerfectReflectionSkill : ReflectionSkill(
    id = "perfect_reflection",
    cooldown = 5,
    rarity = SkillRarity.EPIC,
    duration = 2
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
        player.heal(amount / 10)
        player.sendMessage(message("success", time?.let { " ${it / 1000.0}s" } ?: ""), true)
        attacker?.run {
            damage(player.damageSources.thorns(player), amount * 1.5f)
            velocity = pos.subtract(player.pos).normalize().multiply(1.5, 0.0, 1.5).add(0.0, 0.5, 0.0)
            velocityDirty = true
        }
        return true
    }
}