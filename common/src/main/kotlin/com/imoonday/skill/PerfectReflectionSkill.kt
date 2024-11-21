package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.playSound
import com.imoonday.util.translateSkill
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.*

class PerfectReflectionSkill : Skill(
    id = "perfect_reflection",
    types = listOf(SkillType.DEFENSE),
    cooldown = 5,
    rarity = Rarity.EPIC
), DamageTrigger, ReflectionTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override fun getPersistTime(): Int = 2

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
        player.sendMessage(
            translateSkill(
                id.path, "success",
                time?.let { " ${it / 1000.0}s" } ?: ""), true)
        attacker?.run {
            damage(player.damageSources.thorns(player), amount * 1.5f)
            velocityDirty = true
            velocity = pos.subtract(player.pos).normalize().multiply(1.5, 0.0, 1.5).add(0.0, 0.5, 0.0)
        }
        return true
    }
}