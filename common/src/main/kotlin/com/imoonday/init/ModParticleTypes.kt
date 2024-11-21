package com.imoonday.init

import com.imoonday.*
import com.imoonday.particle.*
import dev.architectury.registry.client.particle.*
import dev.architectury.registry.registries.*
import net.minecraft.particle.*
import net.minecraft.registry.*

object ModParticleTypes {

    @JvmField
    val PARTICLE_TYPES: DeferredRegister<ParticleType<*>> = DeferredRegister.create(MOD_ID, RegistryKeys.PARTICLE_TYPE)

    @JvmField
    val LASER_BEAM: RegistrySupplier<LaserBeamParticle.Type> =
        PARTICLE_TYPES.register("laser_beam", LaserBeamParticle::Type)

    fun init() {
        PARTICLE_TYPES.register()
    }

    fun initClient() {
        ParticleProviderRegistry.register(LASER_BEAM, LaserBeamParticle::Factory)
    }
}