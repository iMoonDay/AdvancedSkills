package com.imoonday.init

import com.imoonday.AdvancedSkills
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object ModSounds {

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

    fun init() = Unit

    fun register(name: String): SoundEvent {
        val id = AdvancedSkills.id(name)
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id))
    }
}