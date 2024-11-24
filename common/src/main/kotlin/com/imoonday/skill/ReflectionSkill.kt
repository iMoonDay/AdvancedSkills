package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

abstract class ReflectionSkill(
    id: String,
    types: List<SkillType> = listOf(SkillType.DEFENSE),
    cooldown: Int,
    rarity: Rarity,
    duration: Int
) : Skill(
    id = id,
    types = types,
    cooldown = cooldown,
    rarity = rarity
), DamageTrigger, ReflectionTrigger {

    override val persistTime: Int = duration

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    protected fun reflectedFailed(player: ServerPlayerEntity) {
        player.sendMessage(translateSkill("extreme_reflection", "failed"), true)
    }

    protected fun reflect(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        attacker?.damage(player.damageSources.thorns(player), amount)?.let {
            player.sendMessage(
                translateSkill("extreme_reflection", if (it) "success" else "failed"),
                true
            )
        }
    }
}