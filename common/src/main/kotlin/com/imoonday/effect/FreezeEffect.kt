package com.imoonday.effect

import net.minecraft.entity.effect.*
import java.awt.*

class FreezeEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.CYAN.rgb
), SyncClientEffect {

    override val syncId: String = "frozen"
}