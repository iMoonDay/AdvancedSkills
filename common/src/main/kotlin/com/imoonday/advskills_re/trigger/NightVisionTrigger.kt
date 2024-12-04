package com.imoonday.advskills_re.trigger

import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*

interface NightVisionTrigger : StatusEffectTrigger {

    fun hasNightVision(player: PlayerEntity): Boolean = player.isUsing()

    override fun shouldHaveStatusEffect(player: PlayerEntity, effect: StatusEffect): Boolean =
        if (effect == StatusEffects.NIGHT_VISION) hasNightVision(player)
        else super.shouldHaveStatusEffect(player, effect)
}