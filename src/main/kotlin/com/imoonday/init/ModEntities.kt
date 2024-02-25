package com.imoonday.init

import com.imoonday.AdvancedSkills
import com.imoonday.entities.SilenceEnergyBallEntity
import com.imoonday.entities.renderer.SilenceEnergyBallEntityRenderer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry


object ModEntities {

    @JvmField
    val SILENCE_ENERGY_BALL: EntityType<SilenceEnergyBallEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::SilenceEnergyBallEntity)
            .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .build()
            .register("silence_energy_ball")

    fun <T : Entity> EntityType<T>.register(name: String): EntityType<T> =
        Registry.register(Registries.ENTITY_TYPE, AdvancedSkills.id(name), this)

    fun <T : LivingEntity> EntityType<T>.register(
        name: String,
        attributeContainer: DefaultAttributeContainer,
    ): EntityType<T> {
        Registry.register(Registries.ENTITY_TYPE, AdvancedSkills.id(name), this)
        FabricDefaultAttributeRegistry.register(this, attributeContainer)
        return this
    }

    fun init() = Unit

    fun initClient() {
        EntityRendererRegistry.register(SILENCE_ENERGY_BALL, ::SilenceEnergyBallEntityRenderer)
    }
}