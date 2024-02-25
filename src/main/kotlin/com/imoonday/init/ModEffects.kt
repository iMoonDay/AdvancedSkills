package com.imoonday.init

import com.imoonday.AdvancedSkills
import com.imoonday.effects.DisarmEffect
import com.imoonday.effects.SilenceEffect
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModEffects {

    @JvmField
    val DISARM = DisarmEffect().register("disarm")

    @JvmField
    val SILENCE = SilenceEffect().register("silence")

    fun <T : StatusEffect> T.register(id: String): T =
        Registry.register(Registries.STATUS_EFFECT, AdvancedSkills.id(id), this)

    fun init() = Unit
}

fun LivingEntity.isDisarmed(): Boolean = hasStatusEffect(ModEffects.DISARM)

fun LivingEntity.isSilenced(): Boolean = hasStatusEffect(ModEffects.SILENCE)