package com.imoonday.init

import com.imoonday.*
import com.imoonday.component.*
import com.imoonday.effect.*
import dev.architectury.registry.registries.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.nbt.*
import net.minecraft.registry.*
import java.util.function.*

object ModEffects {

    @JvmField
    val EFFECTS = DeferredRegister.create(MOD_ID, RegistryKeys.STATUS_EFFECT)

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

    fun <T : StatusEffect> T.register(id: String): RegistrySupplier<T> {
        if (this is SyncClientEffect) SYNC_CLIENT_EFFECTS.add(this)
        return EFFECTS.register(id) { this }
    }

    fun init() = EFFECTS.register()
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

private fun hasStatusEffect(entity: LivingEntity, effect: Supplier<out StatusEffect>): Boolean {
    val statusEffect = effect.get()
    return if (statusEffect is SyncClientEffect) statusEffect.syncId in entity.properties.getList(
        "syncEffects",
        NbtElement.STRING_TYPE.toInt()
    ).map(NbtElement::asString) else entity.hasStatusEffect(statusEffect)
}