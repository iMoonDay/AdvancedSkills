package com.imoonday.init

import com.imoonday.entity.*
import com.imoonday.entity.render.*
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
import net.minecraft.entity.player.PlayerEntity
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

    @JvmStatic
    val ENCHANTED_SWORD: EntityType<EnchantedSwordEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::EnchantedSwordEntity)
            .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
            .trackRangeChunks(8)
            .build()
            .register("enchanted_sword")

    @JvmField
    val TORNADO: EntityType<TornadoEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::TornadoEntity)
            .dimensions(EntityDimensions.changing(1.0f, 2.0f))
            .trackRangeChunks(8)
            .build()
            .register("tornado")

    @JvmField
    val TORNADO_MODEL_LAYER: EntityModelLayer = registerModelLayer("tornado")

    @JvmField
    val HOOK: EntityType<HookEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::HookEntity)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
            .trackRangeChunks(10)
            .trackedUpdateRate(1)
            .build()
            .register("hook")

    @JvmField
    val CLONE_PLAYER: EntityType<ClonePlayerEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::ClonePlayerEntity)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .trackRangeChunks(32)
            .trackedUpdateRate(2)
            .build()
            .register("clone_player", PlayerEntity.createPlayerAttributes().build())

    @JvmField
    val MAGNET: EntityType<MagnetEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::MagnetEntity)
            .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
            .trackRangeChunks(8)
            .fireImmune()
            .build()
            .register("magnet", MagnetEntity.createLivingAttributes().build())

    @JvmField
    val MAGNET_MODEL_LAYER: EntityModelLayer = registerModelLayer("magnet")

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

    private fun registerModelLayer(id: String): EntityModelLayer = EntityModelLayer(id(id), "main")

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
        EntityRendererRegistry.register(ENCHANTED_SWORD, ::EnchantedSwordEntityRenderer)
        EntityRendererRegistry.register(TORNADO, ::TornadoEntityRenderer)
        EntityRendererRegistry.register(HOOK, ::HookEntityRenderer)
        EntityRendererRegistry.register(CLONE_PLAYER, ClonePlayerEntity::Renderer)
        EntityRendererRegistry.register(MAGNET, ::MagnetEntityRenderer)

        EntityModelLayerRegistry.registerModelLayer(METEORITE_MODEL_LAYER, MeteoriteEntity.Renderer::texturedModelData)
        EntityModelLayerRegistry.registerModelLayer(TORNADO_MODEL_LAYER, TornadoEntityModel::texturedModelData)
        EntityModelLayerRegistry.registerModelLayer(
            MAGNET_MODEL_LAYER,
            MagnetEntityModel::texturedModelData
        )
    }
}