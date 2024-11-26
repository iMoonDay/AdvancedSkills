package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

abstract class ReflectionSkill(
    id: String,
    types: List<SkillType> = listOf(SkillType.DEFENSE),
    cooldown: Int,
    rarity: Rarity,
    duration: Int,
) : Skill(
    id = id,
    types = types,
    cooldown = cooldown,
    rarity = rarity
), DamageTrigger, ReflectionTrigger {

    override val persistTime: Int = duration

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    protected fun reflectedFailed(player: ServerPlayerEntity) {
        player.sendMessage(translate("reflection.failed"), true)
    }

    protected fun reflect(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        attacker?.damage(player.damageSources.thorns(player), amount)?.let {
            player.sendMessage(
                translate("reflection.${if (it) "success" else "failed"}"),
                true
            )
        }
    }
}