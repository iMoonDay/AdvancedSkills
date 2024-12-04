package com.imoonday.advskills_re.trigger

import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*

interface StatusEffectTrigger : SkillTrigger {

    fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean = false

    fun shouldHaveStatusEffect(player: PlayerEntity, effect: StatusEffect): Boolean = false
}