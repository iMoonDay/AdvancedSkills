package com.imoonday.advskills_re.init

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.effect.*
import dev.architectury.registry.registries.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.nbt.*
import net.minecraft.registry.*
import java.util.function.*

object ModEffects {

    @JvmField
    val EFFECTS: DeferredRegister<StatusEffect> = DeferredRegister.create(MOD_ID, RegistryKeys.STATUS_EFFECT)

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

    @JvmField
    val VULNERABLE = VulnerableEffect().register("vulnerable")

    fun <T : StatusEffect> T.register(id: String): RegistrySupplier<T> {
        if (this is SyncClientEffect) SYNC_CLIENT_EFFECTS.add(this)
        return EFFECTS.register(id) { this }
    }

    fun init() = EFFECTS.register()
}

val LivingEntity.isDisarmed: Boolean
    get() = this.hasStatusEffect(ModEffects.DISARM)
val LivingEntity.isSilenced: Boolean
    get() = this.hasStatusEffect(ModEffects.SILENCE)
val LivingEntity.isForceFrozen: Boolean
    get() = this.hasStatusEffect(ModEffects.FREEZE)
val LivingEntity.isConfined: Boolean
    get() = this.hasStatusEffect(ModEffects.CONFINEMENT)
val LivingEntity.isSeriousInjured: Boolean
    get() = this.hasStatusEffect(ModEffects.SERIOUS_INJURY)
val LivingEntity.isVulnerable: Boolean
    get() = this.hasStatusEffect(ModEffects.VULNERABLE)

val LivingEntity.vulnerableLevel: Int
    get() = (getStatusEffect(ModEffects.VULNERABLE.get())?.amplifier?.plus(1))
        ?: this.getSyncEffectData(ModEffects.VULNERABLE).getInt("level")

private fun LivingEntity.hasStatusEffect(effect: Supplier<out StatusEffect>): Boolean {
    val statusEffect = effect.get()
    return if (statusEffect is SyncClientEffect) statusEffect.syncId in properties.getCompound("syncEffects").keys
    else hasStatusEffect(statusEffect)
}

private fun LivingEntity.getSyncEffectData(effect: Supplier<out SyncClientEffect>): NbtCompound =
    properties.getCompound("syncEffects").getCompound(effect.get().syncId)