package com.imoonday.client

import com.imoonday.advanced_skills_re.api.*
import com.imoonday.client.render.*
import com.imoonday.entity.render.*
import com.imoonday.entity.render.feature.*
import com.imoonday.init.ModEntities.CLONE_PLAYER
import com.imoonday.init.ModEntities.ENCHANTED_SWORD
import com.imoonday.init.ModEntities.FREEZE_ENERGY_BALL
import com.imoonday.init.ModEntities.HOOK
import com.imoonday.init.ModEntities.MAGNET
import com.imoonday.init.ModEntities.METEORITE
import com.imoonday.init.ModEntities.SERVANT_SKELETON
import com.imoonday.init.ModEntities.SERVANT_WITHER_SKELETON
import com.imoonday.init.ModEntities.SILENCE_ENERGY_BALL
import com.imoonday.init.ModEntities.SLOWNESS_ENERGY_BALL
import com.imoonday.init.ModEntities.SPECIAL_TAME_HORSE
import com.imoonday.init.ModEntities.TORNADO
import com.imoonday.init.ModEntities.UNGROUNDED_ARROW
import com.imoonday.init.ModEntities.UNSTABLE_TNT
import com.imoonday.network.*
import com.imoonday.network.c2s.*
import com.imoonday.skill.*
import com.imoonday.trigger.*
import com.imoonday.util.*
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
    }

    private fun registerClientEvents() {
        ClientGuiEvent.RENDER_HUD.register { context, _ ->
            clientPlayer?.run {
                Skill.getTriggers<SpecialStateRenderTrigger> { it.isInSpecialState(this) }
                    .forEach { it.renderSpecialState(context) }
            }
            SkillSlotRenderer.render(client!!, context)
            Skill.getTriggers<HudRenderTrigger>().forEach { it.render(context) }
            Skill.getTriggers<CrosshairTrigger> { it.shouldRender() && it.getPriority() < 0 }
                .minByOrNull(CrosshairTrigger::getPriority)
                ?.render(context)
            Skill.getTriggers<CrosshairTrigger> { it.shouldRender() && it.getPriority() >= 0 }
                .maxByOrNull(CrosshairTrigger::getPriority)
                ?.render(context)
        }
        LivingEntityFeatureRenderEvent.EVENT.register { _, renderer, helper, context ->
            helper.register(StatusEffectLayer(renderer, context))
            helper.register(IceLayer(renderer, context))
            if (renderer is PlayerEntityRenderer) Skill.getTriggers<FeatureRendererTrigger>()
                .forEach { helper.register(SkillLayer(renderer, context, it)) }
            if (renderer is LivingEntityRenderer) Skill.getTriggers<TargetRenderTrigger>()
                .forEach { helper.register(TargetLayer(renderer, context, it)) }
        }
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            Skill.getTriggers<WorldRendererTrigger>().forEach { it.renderAfterEntities(context) }
        }
        WorldRenderEvents.LAST.register { context ->
            Skill.getTriggers<WorldRendererTrigger>().forEach { it.renderLast(context) }
        }
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register {
            Channels.REQUEST_SYNC_DATA_C2S.sendToServer(RequestSyncDataC2SRequest())
        }
    }
}