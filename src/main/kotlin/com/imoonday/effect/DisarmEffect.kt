package com.imoonday.effect

import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import java.awt.Color

class DisarmEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.RED.rgb
), SyncClientEffect {
    override val syncId: String = "disarm"
}