package com.imoonday.init

import com.imoonday.components.status
import com.imoonday.effects.ConfinementEffect
import com.imoonday.effects.DisarmEffect
import com.imoonday.effects.FreezeEffect
import com.imoonday.effects.SilenceEffect
import com.imoonday.utils.id
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModEffects {

    @JvmField
    val DISARM = DisarmEffect().register("disarm")

    @JvmField
    val SILENCE = SilenceEffect().register("silence")

    @JvmField
    val FREEZE = FreezeEffect().register("freeze")

    @JvmField
    val CONFINEMENT = ConfinementEffect().register("confinement")

    fun <T : StatusEffect> T.register(id: String): T =
        Registry.register(Registries.STATUS_EFFECT, id(id), this)

    fun init() = Unit
}

val LivingEntity.isDisarmed: Boolean
    get() = hasStatusEffect(ModEffects.DISARM)

val LivingEntity.isSilenced: Boolean
    get() = hasStatusEffect(ModEffects.SILENCE)

val LivingEntity.isForceFrozen: Boolean
    get() = if (!world.isClient) hasStatusEffect(ModEffects.FREEZE) else status.getBoolean("frozen")

val LivingEntity.isConfined: Boolean
    get() = hasStatusEffect(ModEffects.CONFINEMENT)