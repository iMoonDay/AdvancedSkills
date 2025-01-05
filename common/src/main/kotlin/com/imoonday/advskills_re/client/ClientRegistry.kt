package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.client.render.entity.*
import com.imoonday.advskills_re.client.render.entity.feature.*
import com.imoonday.advskills_re.client.render.entity.model.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.init.ModEntities.CLONE_PLAYER
import com.imoonday.advskills_re.init.ModEntities.ENCHANTED_SWORD
import com.imoonday.advskills_re.init.ModEntities.FREEZE_ENERGY_BALL
import com.imoonday.advskills_re.init.ModEntities.HOOK
import com.imoonday.advskills_re.init.ModEntities.MAGNET
import com.imoonday.advskills_re.init.ModEntities.METEORITE
import com.imoonday.advskills_re.init.ModEntities.SERVANT_SKELETON
import com.imoonday.advskills_re.init.ModEntities.SERVANT_WITHER_SKELETON
import com.imoonday.advskills_re.init.ModEntities.SILENCE_ENERGY_BALL
import com.imoonday.advskills_re.init.ModEntities.SLOWNESS_ENERGY_BALL
import com.imoonday.advskills_re.init.ModEntities.SPECIAL_TAME_HORSE
import com.imoonday.advskills_re.init.ModEntities.TORNADO
import com.imoonday.advskills_re.init.ModEntities.UNGROUNDED_ARROW
import com.imoonday.advskills_re.init.ModEntities.UNSTABLE_TNT
import com.imoonday.advskills_re.init.ModEntities.VULNERABLE_ENERGY_BALL
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.util.*
import dev.architectury.event.events.client.*
import dev.architectury.registry.client.level.entity.*
import net.fabricmc.api.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.model.*

@Environment(EnvType.CLIENT)
object ClientRegistry {

    @JvmField
    val METEORITE_MODEL_LAYER: EntityModelLayer = registerModelLayer("meteorite")

    @JvmField
    val TORNADO_MODEL_LAYER: EntityModelLayer = registerModelLayer("tornado")

    @JvmField
    val MAGNET_MODEL_LAYER: EntityModelLayer = registerModelLayer("magnet")

    private fun registerModelLayer(id: String): EntityModelLayer = EntityModelLayer(id(id), "main")

    fun register() {
        registerEntities()
        registerClientEvents()
    }

    private fun registerEntities() {
        registerEntityRenderers()
        registerEntityModelLayers()
    }

    private fun registerEntityModelLayers() {
        EntityModelLayerRegistry.register(METEORITE_MODEL_LAYER, MeteoriteEntityRenderer::texturedModelData)
        EntityModelLayerRegistry.register(TORNADO_MODEL_LAYER, TornadoEntityModel::texturedModelData)
        EntityModelLayerRegistry.register(MAGNET_MODEL_LAYER, MagnetEntityModel::texturedModelData)
    }

    private fun registerEntityRenderers() {
        EntityRendererRegistry.register(SILENCE_ENERGY_BALL, ::SilenceEnergyBallEntityRenderer)
        EntityRendererRegistry.register(UNSTABLE_TNT, ::TntEntityRenderer)
        EntityRendererRegistry.register(FREEZE_ENERGY_BALL, ::FreezeEnergyBallEntityRenderer)
        EntityRendererRegistry.register(SLOWNESS_ENERGY_BALL, ::SlownessEnergyBallEntityRenderer)
        EntityRendererRegistry.register(SPECIAL_TAME_HORSE, ::HorseEntityRenderer)
        EntityRendererRegistry.register(SERVANT_SKELETON, ::SkeletonEntityRenderer)
        EntityRendererRegistry.register(SERVANT_WITHER_SKELETON, ::WitherSkeletonEntityRenderer)
        EntityRendererRegistry.register(METEORITE, ::MeteoriteEntityRenderer)
        EntityRendererRegistry.register(ENCHANTED_SWORD, ::EnchantedSwordEntityRenderer)
        EntityRendererRegistry.register(TORNADO, ::TornadoEntityRenderer)
        EntityRendererRegistry.register(HOOK, ::HookEntityRenderer)
        EntityRendererRegistry.register(CLONE_PLAYER, ::ClonePlayerEntityRenderer)
        EntityRendererRegistry.register(MAGNET, ::MagnetEntityRenderer)
        EntityRendererRegistry.register(UNGROUNDED_ARROW, ::ArrowEntityRenderer)
        EntityRendererRegistry.register(VULNERABLE_ENERGY_BALL, ::VulnerableEnergyBallEntityRenderer)
    }

    private fun registerClientEvents() {
        ClientGuiEvent.RENDER_HUD.register { context, _ ->
            if (client?.options?.hudHidden == true) return@register

            SkillRendererHandler.renderOverlay(context)
            if (ClientConfig.get().hideSkillSlots != SkillSlotRenderer.HideMode.HIDE) {
                SkillSlotRenderer.render(context)
            }
            SkillSlotRenderer.renderSelectedSkill(context)
            SkillRendererHandler.renderHud(context)
            SkillRendererHandler.renderCrosshair(context)
        }
        LivingEntityFeatureRenderEvent.EVENT.register { _, renderer, helper, context ->
            helper.register(IceLayer(renderer, context))
            if (renderer is PlayerEntityRenderer) {
                SkillRendererHandler.forEachPlayerFeatureRenderer { skill, featureRenderer ->
                    helper.register(SkillLayer(skill, renderer, context, featureRenderer))
                }
            }
            if (renderer is LivingEntityRenderer) {
                SkillRendererHandler.forEachLivingFeatureRenderer { skill, featureRenderer ->
                    helper.register(LivingLayer(skill, renderer, context, featureRenderer))
                }
            }
        }
        WorldRenderEvents.AFTER_ENTITIES.register(SkillRendererHandler::renderAfterEntities)
        WorldRenderEvents.LAST.register(SkillRendererHandler::renderLast)
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register {
            SkillWheelScreen.quickCastSlot = null
            Channels.REQUEST_SYNC_COMPONENT_C2S.sendToServer(
                RequestSyncComponentC2SRequest(
                    it.id,
                    RequestSyncComponentC2SRequest.ComponentType.PLAYER_DATA,
                    RequestSyncComponentC2SRequest.Receiver.ALL_PLAYERS
                )
            )
            Channels.REQUEST_SYNC_COMPONENT_C2S.sendToServer(
                RequestSyncComponentC2SRequest(
                    it.id,
                    RequestSyncComponentC2SRequest.ComponentType.ENTITY_PROPERTIES,
                    RequestSyncComponentC2SRequest.Receiver.ALL_PLAYERS
                )
            )
        }
    }
}