package com.imoonday.advskills_re.effect

import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.effect.*
import net.minecraft.util.math.*
import org.joml.*
import java.awt.*

class FreezeEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.CYAN.rgb
), SyncClientEffect {

    override val syncId: String = "frozen"

    override fun onApplied(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        super.onApplied(entity, attributes, amplifier)
        entity.velocity = Vec3d.ZERO
    }

    override fun canApplyUpdateEffect(duration: Int, amplifier: Int): Boolean = true

    override fun applyUpdateEffect(entity: LivingEntity, amplifier: Int) {
        super.applyUpdateEffect(entity, amplifier)
        entity.velocity = Vec3d.ZERO
    }

    override fun onRemoved(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        super.onRemoved(entity, attributes, amplifier)
        entity.velocity = Vec3d.ZERO
    }
}