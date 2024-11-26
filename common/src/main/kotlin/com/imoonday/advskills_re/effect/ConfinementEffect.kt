package com.imoonday.advskills_re.effect

import net.minecraft.entity.effect.*
import java.awt.*

class ConfinementEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.ORANGE.rgb
), SyncClientEffect {

    override val syncId: String = "confinement"
}