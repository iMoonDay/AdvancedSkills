package com.imoonday.trigger

import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity

interface StatusEffectTrigger : SkillTrigger {

    fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean = false
}