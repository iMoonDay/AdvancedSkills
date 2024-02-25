package com.imoonday.effects

import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import java.awt.Color

class SilenceEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.ORANGE.rgb
)