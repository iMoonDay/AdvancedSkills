package com.imoonday.init

import com.imoonday.*
import com.imoonday.entity.*
import com.imoonday.entity.render.*
import com.imoonday.util.*
import dev.architectury.registry.client.level.entity.*
import dev.architectury.registry.level.entity.*
import dev.architectury.registry.registries.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*

object ModEntities {

    @JvmField
    val ENTITIES: DeferredRegister<EntityType<*>> = DeferredRegister.create(MOD_ID, RegistryKeys.ENTITY_TYPE)

    @JvmField
    val SILENCE_ENERGY_BALL: RegistrySupplier<EntityType<SilenceEnergyBallEntity>> =
        EntityType.Builder.create(::SilenceEnergyBallEntity, SpawnGroup.MISC)
            .setDimensions(1.0f, 1.0f)
            .maxTrackingRange(4)
            .trackingTickInterval(10)
            .register("silence_energy_ball")

    @JvmField
    val UNSTABLE_TNT: RegistrySupplier<EntityType<UnstableTntEntity>> =
        EntityType.Builder.create(::UnstableTntEntity, SpawnGroup.MISC)
            .setDimensions(0.98f, 0.98f)
            .makeFireImmune()
            .maxTrackingRange(10)
            .trackingTickInterval(10)
            .register("unstable_tnt")

    @JvmField
    val FREEZE_ENERGY_BALL: RegistrySupplier<EntityType<FreezeEnergyBallEntity>> =
        EntityType.Builder.create(::FreezeEnergyBallEntity, SpawnGroup.MISC)
            .setDimensions(1.0f, 1.0f)
            .maxTrackingRange(4)
            .trackingTickInterval(10)
            .register("freeze_energy_ball")

    @JvmField
    val SLOWNESS_ENERGY_BALL: RegistrySupplier<EntityType<SlownessEnergyBallEntity>> =
        EntityType.Builder.create(::SlownessEnergyBallEntity, SpawnGroup.MISC)
            .setDimensions(1.0f, 1.0f)
            .maxTrackingRange(4)
            .trackingTickInterval(10)
            .register("slowness_energy_ball")

    @JvmField
    val SPECIAL_TAME_HORSE: RegistrySupplier<EntityType<SpecialTameHorseEntity>> =
        EntityType.Builder.create(::SpecialTameHorseEntity, SpawnGroup.CREATURE)
            .setDimensions(1.3964844f, 1.6f)
            .maxTrackingRange(10)
            .register("special_tame_horse", AbstractHorseEntity.createBaseHorseAttributes())

    @JvmField
    val SERVANT_SKELETON: RegistrySupplier<EntityType<ServantSkeletonEntity>> =
        EntityType.Builder.create(::ServantSkeletonEntity, SpawnGroup.CREATURE)
            .setDimensions(0.6f, 1.99f)
            .maxTrackingRange(8)
            .register("servant_skeleton", ServantSkeletonEntity.createAttributes())

    @JvmStatic
    val SERVANT_WITHER_SKELETON: RegistrySupplier<EntityType<ServantWitherSkeletonEntity>> =
        EntityType.Builder.create(::ServantWitherSkeletonEntity, SpawnGroup.CREATURE)
            .setDimensions(0.7f, 2.4f)
            .makeFireImmune()
            .maxTrackingRange(8)
            .register("servant_wither_skeleton", ServantWitherSkeletonEntity.createAttributes())

    @JvmField
    val METEORITE: RegistrySupplier<EntityType<MeteoriteEntity>> =
        EntityType.Builder.create(::MeteoriteEntity, SpawnGroup.MISC)
            .setDimensions(1f, 1f)
            .maxTrackingRange(8)
            .trackingTickInterval(1)
            .register("meteorite")

    @JvmField
    val METEORITE_MODEL_LAYER: EntityModelLayer = registerModelLayer("meteorite")

    @JvmStatic
    val ENCHANTED_SWORD: RegistrySupplier<EntityType<EnchantedSwordEntity>> =
        EntityType.Builder.create(::EnchantedSwordEntity, SpawnGroup.MISC)
            .setDimensions(0.5f, 0.5f)
            .maxTrackingRange(8)
            .register("enchanted_sword")

    @JvmField
    val TORNADO: RegistrySupplier<EntityType<TornadoEntity>> =
        EntityType.Builder.create(::TornadoEntity, SpawnGroup.MISC)
            .setDimensions(1.0f, 2.0f)
            .maxTrackingRange(8)
            .register("tornado")

    @JvmField
    val TORNADO_MODEL_LAYER: EntityModelLayer = registerModelLayer("tornado")

    @JvmField
    val HOOK: RegistrySupplier<EntityType<HookEntity>> =
        EntityType.Builder.create(::HookEntity, SpawnGroup.MISC)
            .setDimensions(0.25f, 0.25f)
            .maxTrackingRange(10)
            .trackingTickInterval(1)
            .register("hook")

    @JvmField
    val CLONE_PLAYER: RegistrySupplier<EntityType<ClonePlayerEntity>> =
        EntityType.Builder.create(::ClonePlayerEntity, SpawnGroup.MISC)
            .setDimensions(0.6f, 1.8f)
            .maxTrackingRange(32)
            .trackingTickInterval(2)
            .register("clone_player", PlayerEntity.createPlayerAttributes())

    @JvmField
    val MAGNET: RegistrySupplier<EntityType<MagnetEntity>> =
        EntityType.Builder.create(::MagnetEntity, SpawnGroup.MISC)
            .setDimensions(0.5f, 0.5f)
            .maxTrackingRange(8)
            .makeFireImmune()
            .register("magnet", MagnetEntity.createLivingAttributes())

    @JvmField
    val MAGNET_MODEL_LAYER: EntityModelLayer = registerModelLayer("magnet")

    fun <T : Entity> EntityType.Builder<T>.register(name: String): RegistrySupplier<EntityType<T>> =
        ENTITIES.register(name) { this.build(name) }

    fun <T : LivingEntity> EntityType.Builder<T>.register(
        name: String,
        attributeContainer: DefaultAttributeContainer.Builder,
    ): RegistrySupplier<EntityType<T>> {
        val supplier = register(name)
        EntityAttributeRegistry.register(supplier) { attributeContainer }
        return supplier
    }

    private fun registerModelLayer(id: String): EntityModelLayer = EntityModelLayer(id(id), "main")

    fun init() = ENTITIES.register()

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

        EntityModelLayerRegistry.register(METEORITE_MODEL_LAYER, MeteoriteEntity.Renderer::texturedModelData)
        EntityModelLayerRegistry.register(TORNADO_MODEL_LAYER, TornadoEntityModel::texturedModelData)
        EntityModelLayerRegistry.register(
            MAGNET_MODEL_LAYER,
            MagnetEntityModel::texturedModelData
        )
    }
}