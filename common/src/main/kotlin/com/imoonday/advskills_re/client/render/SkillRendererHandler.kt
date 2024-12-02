package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.client.render.renderer.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
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
    fun <T : Skill> T.registerRenderer(renderer: IRenderer<T>) {
        if (renderer is IOverlayRenderer) overlayRenderers[this] = renderer as IOverlayRenderer<Skill>
        if (renderer is IHudRenderer) hudRenderers[this] = renderer as IHudRenderer<Skill>
        if (renderer is ICrosshairRenderer) crosshairRenderers[this] = renderer as ICrosshairRenderer<Skill>
        if (renderer is IEntityRenderer) entityRenderers[this] = renderer as IEntityRenderer<Skill>
        if (renderer is IPlayerEntityRenderer) playerEntityRenderers[this] = renderer as IPlayerEntityRenderer<Skill>
        if (renderer is IFeatureRenderer) featureRenderers[this] = renderer as IFeatureRenderer<Skill>
        if (renderer is ILivingFeatureRenderer) livingFeatureRenderers[this] =
            renderer as ILivingFeatureRenderer<Skill>
        if (renderer is IPlayerFeatureRenderer) playerFeatureRenderers[this] =
            renderer as IPlayerFeatureRenderer<Skill>
        if (renderer is IWorldRenderer) worldRenderers[this] = renderer as IWorldRenderer<Skill>
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
        Skills.CHARGED_SWEEP.registerSkillAboveHeadRenderer()
        Skills.DOPING.registerSkillAboveHeadRenderer()
        Skills.ITEM_ATTRACTION.registerSkillAboveHeadRenderer()
        Skills.METEOR_SHOWER.registerSkillAboveHeadRenderer()
        Skills.TAUNT.registerSkillAboveHeadRenderer()
        Skills.TIME_REWIND.registerSkillAboveHeadRenderer()
        Skills.WATER_WALKER.registerSkillAboveHeadRenderer()
        Skills.WIND_BLADE.registerSkillAboveHeadRenderer()
        Skills.RETURN.registerSkillAboveHeadRenderer()
        Skills.LAVA_WALKER.registerSkillAboveHeadRenderer()

        Skills.ABSOLUTE_DEFENSE.registerSkillAroundRenderer()
        Skills.ACTIVE_DEFENSE.registerSkillAroundRenderer()
        Skills.DAMAGE_ABSORPTION.registerSkillAroundRenderer()
        Skills.MICRO_REFLECTION.registerSkillAroundRenderer()
        Skills.RAPID_REFLECTION.registerSkillAroundRenderer()
        Skills.EXTREME_REFLECTION.registerSkillAroundRenderer()
        Skills.PERFECT_REFLECTION.registerSkillAroundRenderer()
        Skills.NEGATIVE_RESISTANCE.registerSkillAroundRenderer()

        Skills.PRIMARY_FREEZE.registerRenderer(PrimaryFreezeSkillRenderer())
        Skills.DISGUISE.registerRenderer(DisguiseSkillRenderer())
        Skills.BLOOD_SEAL.registerRenderer(BloodSealSkillRenderer())
        Skills.GRAPPLING_HOOK.registerRenderer(GrapplingHookSkillRenderer())
        Skills.PRIMARY_CONFINEMENT.registerRenderer(PrimaryConfinementSkillRenderer())
        Skills.ORE_PERCEPTION.registerRenderer(OrePerceptionSkillRenderer())
        Skills.INSIGHTFUL_EYE.registerRenderer(InsightfulEyeSkillRenderer())
        Skills.SWORD_SOUL_GUARDING.registerRenderer(SwordSoulGuardingSkillRenderer())
    }

    private fun <T> T.registerSkillAboveHeadRenderer() where T : Skill, T : UsingRenderTrigger =
        this.registerRenderer(SkillAboveHeadRenderer.create())

    private fun <T> T.registerSkillAroundRenderer() where T : Skill, T : UsingRenderTrigger =
        this.registerRenderer(SkillAroundRenderer.create())
}