package com.imoonday.advskills_re.effect

import net.minecraft.entity.effect.*
import java.awt.*

class WeakenedEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    4738376
), SyncClientEffect {

    override val syncId: String = "weakened"
}