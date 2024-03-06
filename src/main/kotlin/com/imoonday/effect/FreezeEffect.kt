package com.imoonday.effect

import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import java.awt.Color

class FreezeEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.CYAN.rgb
), SyncClientEffect {
    override val syncId: String = "frozen"
}