package com.imoonday.effect

import net.minecraft.entity.effect.*
import java.awt.*

class SilenceEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.ORANGE.rgb
), SyncClientEffect {

    override val syncId: String = "silence"
}