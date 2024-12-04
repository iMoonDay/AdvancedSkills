package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.renderer.*
import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.util.math.*

interface ITargetRenderer<T> : ILivingFeatureRenderer<T> where T : Skill, T : TargetRenderTrigger {

    override fun <E : LivingEntity, M : EntityModel<E>> render(
        skill: T,
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        entity: E,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<E, M>,
        context: EntityRendererFactory.Context
    ) {
        val player = clientPlayer ?: return
        if (!skill.isTarget(player, entity)) return

        renderIndicator(matrices, context, provider, entity)
    }

    fun renderIndicator(
        matrices: MatrixStack,
        context: EntityRendererFactory.Context,
        provider: VertexConsumerProvider,
        entity: Entity,
    ) {
        matrices.push()
        matrices.translate(1f, 1.5f, -1f)
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f))
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.disableCull()
        RenderSystem.setShaderTexture(0, indicatorTexture)
        Renderer2d.renderTexture(matrices, 0.0, 0.0, 2.0, 2.0, 0f, 0f, 16.0, 16.0, 16.0, 16.0)
        RenderSystem.disableDepthTest()
        RenderSystem.disableBlend()
        RenderSystem.enableCull()
        matrices.pop()
    }

    companion object {

        private val indicatorTexture = id("indicator.png")

        fun <T> create(): ITargetRenderer<T> where T : Skill, T : TargetRenderTrigger = object : ITargetRenderer<T> {}
    }
}