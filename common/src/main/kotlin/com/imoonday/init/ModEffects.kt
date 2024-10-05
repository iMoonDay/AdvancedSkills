package com.imoonday.init

import com.imoonday.component.properties
import com.imoonday.effect.*
import com.imoonday.util.id
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.nbt.NbtElement
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModEffects {

    @JvmField
    val SYNC_CLIENT_EFFECTS: MutableList<SyncClientEffect> = mutableListOf()

    @JvmField
    val DISARM = DisarmEffect().register("disarm")

    @JvmField
    val SILENCE = SilenceEffect().register("silence")

    @JvmField
    val FREEZE = FreezeEffect().register("freeze")

    @JvmField
    val CONFINEMENT = ConfinementEffect().register("confinement")

    @JvmField
    val SERIOUS_INJURY = SeriousInjuryEffect().register("serious_injury")

    fun <T : StatusEffect> T.register(id: String): T {
        if (this is SyncClientEffect) SYNC_CLIENT_EFFECTS.add(this)
        return Registry.register(Registries.STATUS_EFFECT, id(id), this)
    }

    fun init() = Unit
}

val LivingEntity.isDisarmed: Boolean
    get() = hasStatusEffect(this, ModEffects.DISARM)
val LivingEntity.isSilenced: Boolean
    get() = hasStatusEffect(this, ModEffects.SILENCE)
val LivingEntity.isForceFrozen: Boolean
    get() = hasStatusEffect(this, ModEffects.FREEZE)
val LivingEntity.isConfined: Boolean
    get() = hasStatusEffect(this, ModEffects.CONFINEMENT)
val LivingEntity.isSeriousInjured: Boolean
    get() = hasStatusEffect(this, ModEffects.SERIOUS_INJURY)

private fun hasStatusEffect(entity: LivingEntity, effect: StatusEffect): Boolean =
    if (effect is SyncClientEffect) effect.syncId in entity.properties.getList(
        "syncEffects",
        NbtElement.STRING_TYPE.toInt()
    ).map { it.asString() } else entity.hasStatusEffect(effect)