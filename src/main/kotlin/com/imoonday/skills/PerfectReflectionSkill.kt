package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.stopCooling
import com.imoonday.components.stopUsingSkill
import com.imoonday.triggers.PlayerDamageTrigger
import com.imoonday.triggers.ReflectionTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.translateSkill
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Util

class PerfectReflectionSkill : Skill(
    id = "perfect_reflection",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 5,
    rarity = Rarity.EPIC
), PlayerDamageTrigger, ReflectionTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override val persistTime: Int = 2
    override val skill: Skill
        get() = this

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsingSkill(this)) return amount
        val time = getStartTime(player)?.let {
            Util.getMeasuringTimeMs() - it
        }
        player.stopUsingSkill(skill)
        player.stopCooling(skill)
        player.world.playSound(null, player.blockPos, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS)
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
        return 0.0f
    }
}