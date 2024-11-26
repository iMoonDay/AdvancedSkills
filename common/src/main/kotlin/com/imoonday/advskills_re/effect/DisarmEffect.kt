package com.imoonday.advskills_re.effect

import net.minecraft.entity.effect.*
import java.awt.*

class DisarmEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.RED.rgb
), SyncClientEffect {

    override val syncId: String = "disarm"
}