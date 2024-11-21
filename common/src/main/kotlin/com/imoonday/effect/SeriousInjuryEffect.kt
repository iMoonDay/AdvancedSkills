package com.imoonday.effect

import com.imoonday.component.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.effect.*
import java.awt.*

class SeriousInjuryEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.RED.rgb
), SyncClientEffect {

    override val syncId: String = "serious_injury"

    override fun canApplyUpdateEffect(duration: Int, amplifier: Int): Boolean = true

    override fun applyUpdateEffect(entity: LivingEntity, amplifier: Int) {
        super.applyUpdateEffect(entity, amplifier)
        if (!entity.properties.contains("sealedHealth")) entity.properties.putFloat("sealedHealth", entity.health)
        val sealedHealth = entity.properties.getFloat("sealedHealth")
        if (entity.health > sealedHealth) {
            val amount = entity.health - sealedHealth
            entity.health = sealedHealth
            entity.properties.putFloat("sealedHealing", entity.properties.getFloat("sealedHealing") + amount)
        }
    }

    override fun onApplied(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        super.onApplied(entity, attributes, amplifier)
        entity.properties.putFloat("sealedHealth", entity.health)
    }

    override fun onRemoved(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        super.onRemoved(entity, attributes, amplifier)
        entity.properties.remove("sealedHealth")
        if (entity.properties.contains("sealedHealing")) {
            val sealedHealing = entity.properties.getFloat("sealedHealing")
            entity.properties.remove("sealedHealing")
            entity.heal(sealedHealing)
        }
    }
}