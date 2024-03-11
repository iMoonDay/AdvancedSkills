package com.imoonday.init

import com.imoonday.entity.*
import com.imoonday.util.id
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.client.render.entity.HorseEntityRenderer
import net.minecraft.client.render.entity.SkeletonEntityRenderer
import net.minecraft.client.render.entity.TntEntityRenderer
import net.minecraft.client.render.entity.WitherSkeletonEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.entity.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.passive.AbstractHorseEntity
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

    @JvmField
    val UNSTABLE_TNT: EntityType<UnstableTntEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::UnstableTntEntity)
            .dimensions(EntityDimensions.fixed(0.98f, 0.98f))
            .fireImmune()
            .trackRangeChunks(10)
            .trackedUpdateRate(10)
            .build()
            .register("unstable_tnt")

    @JvmField
    val FREEZE_ENERGY_BALL: EntityType<FreezeEnergyBallEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::FreezeEnergyBallEntity)
            .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .build()
            .register("freeze_energy_ball")

    @JvmField
    val SLOWNESS_ENERGY_BALL: EntityType<SlownessEnergyBallEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::SlownessEnergyBallEntity)
            .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .build()
            .register("slowness_energy_ball")

    @JvmField
    val SPECIAL_TAME_HORSE: EntityType<SpecialTameHorseEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ::SpecialTameHorseEntity)
            .dimensions(EntityDimensions.fixed(1.3964844f, 1.6f))
            .trackRangeChunks(10)
            .build()
            .register("special_tame_horse", AbstractHorseEntity.createBaseHorseAttributes().build())

    @JvmField
    val SERVANT_SKELETON: EntityType<ServantSkeletonEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ::ServantSkeletonEntity)
            .dimensions(EntityDimensions.fixed(0.6f, 1.99f))
            .trackRangeChunks(8)
            .build()
            .register("servant_skeleton", ServantSkeletonEntity.createAttributes().build())

    @JvmStatic
    val SERVANT_WITHER_SKELETON: EntityType<ServantWitherSkeletonEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ::ServantWitherSkeletonEntity)
            .dimensions(EntityDimensions.fixed(0.7f, 2.4f))
            .fireImmune()
            .trackRangeChunks(8)
            .build()
            .register("servant_wither_skeleton", ServantWitherSkeletonEntity.createAttributes().build())

    @JvmField
    val METEORITE: EntityType<MeteoriteEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::MeteoriteEntity)
            .dimensions(EntityDimensions.changing(1f, 1f))
            .trackRangeChunks(8)
            .trackedUpdateRate(1)
            .forceTrackedVelocityUpdates(true)
            .build()
            .register("meteorite")

    @JvmField
    val METEORITE_MODEL_LAYER: EntityModelLayer = registerModelLayer("meteorite")

    fun <T : Entity> EntityType<T>.register(name: String): EntityType<T> =
        Registry.register(Registries.ENTITY_TYPE, id(name), this)

    fun <T : LivingEntity> EntityType<T>.register(
        name: String,
        attributeContainer: DefaultAttributeContainer,
    ): EntityType<T> {
        Registry.register(Registries.ENTITY_TYPE, id(name), this)
        FabricDefaultAttributeRegistry.register(this, attributeContainer)
        return this
    }

    fun registerModelLayer(id: String): EntityModelLayer = EntityModelLayer(id(id), "main")

    fun init() = Unit

    fun initClient() {
        EntityRendererRegistry.register(SILENCE_ENERGY_BALL, SilenceEnergyBallEntity::Renderer)
        EntityRendererRegistry.register(UNSTABLE_TNT, ::TntEntityRenderer)
        EntityRendererRegistry.register(FREEZE_ENERGY_BALL, FreezeEnergyBallEntity::Renderer)
        EntityRendererRegistry.register(SLOWNESS_ENERGY_BALL, SlownessEnergyBallEntity::Renderer)
        EntityRendererRegistry.register(SPECIAL_TAME_HORSE, ::HorseEntityRenderer)
        EntityRendererRegistry.register(SERVANT_SKELETON, ::SkeletonEntityRenderer)
        EntityRendererRegistry.register(SERVANT_WITHER_SKELETON, ::WitherSkeletonEntityRenderer)
        EntityRendererRegistry.register(METEORITE, MeteoriteEntity::Renderer)

        EntityModelLayerRegistry.registerModelLayer(METEORITE_MODEL_LAYER, MeteoriteEntity.Renderer::texturedModelData)
    }
}