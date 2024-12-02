package com.imoonday.advskills_re.effect

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.effect.*
import net.minecraft.nbt.*
import java.awt.*

class SeriousInjuryEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    Color.RED.rgb
), SyncClientEffect {

    override val syncId: String = "serious_injury"

    override fun canApplyUpdateEffect(duration: Int, amplifier: Int): Boolean = true

    override fun onRemoved(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        super.onRemoved(entity, attributes, amplifier)
        val properties = entity.properties
        if (properties.contains("sealedHealing")) {
            val sealedHealing = properties.getFloat("sealedHealing")
            properties.remove("sealedHealing")
            properties.putBoolean("removingSeriousInjury", true)
            entity.syncProperties()
            entity.heal(sealedHealing)
        }
    }

    companion object {

        fun onSetHealth(entity: LivingEntity, health: Float): Boolean {
            val result = entity.isSeriousInjured && health > entity.health
            if (result) {
                val properties = entity.properties
                val isRemoving = properties.contains(
                    "removingSeriousInjury",
                    NbtElement.BYTE_TYPE.toInt()
                ) && properties.getBoolean("removingSeriousInjury")
                if (isRemoving) {
                    properties.remove("removingSeriousInjury")
                    entity.syncProperties()
                    return false
                }
                properties.putFloat(
                    "sealedHealing",
                    properties.getFloat("sealedHealing") + (health - entity.health)
                )
                entity.syncProperties()
            }
            return result
        }
    }
}