package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.client.render.renderer.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.client.gui.*
import net.minecraft.client.network.*
import net.minecraft.client.render.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*

object SkillRendererHandler {

    private val overlayRenderers: MutableMap<Skill, IOverlayRenderer<Skill>> = linkedMapOf()
    private val hudRenderers: MutableMap<Skill, IHudRenderer<Skill>> = linkedMapOf()
    private val crosshairRenderers: MutableMap<Skill, ICrosshairRenderer<Skill>> = linkedMapOf()
    private val entityRenderers: MutableMap<Skill, IEntityRenderer<Skill>> = linkedMapOf()
    private val playerEntityRenderers: MutableMap<Skill, IPlayerEntityRenderer<Skill>> = linkedMapOf()
    private val featureRenderers: MutableMap<Skill, IFeatureRenderer<Skill>> = linkedMapOf()
    private val livingFeatureRenderers: MutableMap<Skill, ILivingFeatureRenderer<Skill>> = linkedMapOf()
    private val playerFeatureRenderers: MutableMap<Skill, IPlayerFeatureRenderer<Skill>> = linkedMapOf()
    private val worldRenderers: MutableMap<Skill, IWorldRenderer<Skill>> = linkedMapOf()

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : Skill> registerRenderer(skill: T, renderer: IRenderer<T>) {
        if (renderer is IOverlayRenderer) overlayRenderers[skill] = renderer as IOverlayRenderer<Skill>
        if (renderer is IHudRenderer) hudRenderers[skill] = renderer as IHudRenderer<Skill>
        if (renderer is ICrosshairRenderer) crosshairRenderers[skill] = renderer as ICrosshairRenderer<Skill>
        if (renderer is IEntityRenderer) entityRenderers[skill] = renderer as IEntityRenderer<Skill>
        if (renderer is IPlayerEntityRenderer) playerEntityRenderers[skill] = renderer as IPlayerEntityRenderer<Skill>
        if (renderer is IFeatureRenderer) featureRenderers[skill] = renderer as IFeatureRenderer<Skill>
        if (renderer is ILivingFeatureRenderer) livingFeatureRenderers[skill] =
            renderer as ILivingFeatureRenderer<Skill>
        if (renderer is IPlayerFeatureRenderer) playerFeatureRenderers[skill] =
            renderer as IPlayerFeatureRenderer<Skill>
        if (renderer is IWorldRenderer) worldRenderers[skill] = renderer as IWorldRenderer<Skill>
    }

    fun renderOverlay(drawContext: DrawContext) = overlayRenderers.forEach { it.value.render(it.key, drawContext) }

    fun renderHud(drawContext: DrawContext) = hudRenderers.forEach { it.value.render(it.key, drawContext) }

    fun renderCrosshair(context: DrawContext) {
        val filteredRenderers = crosshairRenderers.filter { (skill, renderer) ->
            renderer.shouldRenderCrosshair(skill)
        }

        val minRenderer = filteredRenderers.minByOrNull { (skill, renderer) -> renderer.getPriority(skill) }
        val maxRenderer = filteredRenderers.maxByOrNull { (skill, renderer) -> renderer.getPriority(skill) }

        minRenderer?.run { value.render(key, context) }
        maxRenderer?.run { value.render(key, context) }
    }

    fun renderEntity(
        camera: Camera,
        entity: Entity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) = entityRenderers.forEach {
        it.value.render(it.key, camera, entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    fun renderPlayerEntity(
        player: AbstractClientPlayerEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ): Boolean = playerEntityRenderers.any {
        it.value.render(it.key, player, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    fun forEachFeatureRenderer(
        action: (Skill, IFeatureRenderer<Skill>) -> Unit
    ) = featureRenderers.forEach { action(it.key, it.value) }

    fun forEachLivingFeatureRenderer(
        action: (Skill, ILivingFeatureRenderer<Skill>) -> Unit
    ) = livingFeatureRenderers.forEach { action(it.key, it.value) }

    fun forEachPlayerFeatureRenderer(
        action: (Skill, IPlayerFeatureRenderer<Skill>) -> Unit
    ) = playerFeatureRenderers.forEach { action(it.key, it.value) }

    fun renderAfterEntities(context: WorldRenderContext) = worldRenderers.forEach {
        it.value.renderAfterEntities(it.key, context)
    }

    fun renderLast(context: WorldRenderContext) = worldRenderers.forEach {
        it.value.renderLast(it.key, context)
    }

    fun register() {
        registerRenderer(Skills.PRIMARY_FREEZE, PrimaryFreezeSkillRenderer())
        registerRenderer(Skills.DISGUISE, DisguiseSkillRenderer())
        registerRenderer(Skills.BLOOD_SEAL, BloodSealSkillRenderer())
        registerRenderer(Skills.GRAPPLING_HOOK, GrapplingHookSkillRenderer())
        registerRenderer(Skills.PRIMARY_CONFINEMENT, PrimaryConfinementSkillRenderer())
        registerRenderer(Skills.CHARGED_SWEEP, SkillAboveHeadRenderer.create())
        registerRenderer(Skills.DOPING, SkillAboveHeadRenderer.create())
        registerRenderer(Skills.ITEM_ATTRACTION, SkillAboveHeadRenderer.create())
        registerRenderer(Skills.METEOR_SHOWER, SkillAboveHeadRenderer.create())
        registerRenderer(Skills.TAUNT, SkillAboveHeadRenderer.create())
        registerRenderer(Skills.TIME_REWIND, SkillAboveHeadRenderer.create())
        registerRenderer(Skills.ABSOLUTE_DEFENSE, SkillAroundRenderer.create())
        registerRenderer(Skills.ACTIVE_DEFENSE, SkillAroundRenderer.create())
        registerRenderer(Skills.DAMAGE_ABSORPTION, SkillAroundRenderer.create())
        registerRenderer(Skills.ORE_PERCEPTION, OrePerceptionSkillRenderer())
        registerRenderer(Skills.INSIGHTFUL_EYE, InsightfulEyeSkillRenderer())
        registerRenderer(Skills.SWORD_SOUL_GUARDING, SwordSoulGuardingSkillRenderer())
    }
}