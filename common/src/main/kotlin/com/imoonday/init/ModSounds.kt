package com.imoonday.init

import com.imoonday.*
import com.imoonday.util.*
import dev.architectury.registry.registries.*
import net.minecraft.registry.*
import net.minecraft.sound.*

object ModSounds {

    @JvmStatic
    val SOUNDS: DeferredRegister<SoundEvent> = DeferredRegister.create(MOD_ID, RegistryKeys.SOUND_EVENT)

    @JvmStatic
    val HEAL = register("heal")

    @JvmStatic
    val PURIFY = register("purify")

    @JvmStatic
    val NOTICE = register("notice")

    @JvmStatic
    val DASH = register("dash")

    @JvmStatic
    val DISARM = register("disarm")

    @JvmStatic
    val PIERCING = register("piercing")

    @JvmStatic
    val FIRE = register("fire")

    @JvmStatic
    val LASER = register("laser")

    fun init() = SOUNDS.register()

    fun register(name: String): RegistrySupplier<SoundEvent>? {
        return SOUNDS.register(name) { SoundEvent.of(id(name)) }
    }
}