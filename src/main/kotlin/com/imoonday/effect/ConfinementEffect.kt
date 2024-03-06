package com.imoonday.effect

import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import java.awt.Color

class ConfinementEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.ORANGE.rgb
), SyncClientEffect {
    override val syncId: String = "confinement"
}