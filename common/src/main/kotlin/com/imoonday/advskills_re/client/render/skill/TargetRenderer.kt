package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.render.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.util.math.*

interface TargetRenderer<T> : IPostLivingEntityRenderer<T> where T : Skill, T : TargetRenderTrigger {

    override fun render(
        skill: T,
        entity: LivingEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val player = clientPlayer ?: return
        if (!skill.isTarget(player, entity)) return

        renderIndicator(matrices, vertexConsumers, entity)
    }

    fun renderIndicator(
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        entity: Entity,
    ) {
        matrices.push()

        val scale = entity.width
        matrices.scale(scale, scale, scale)
        matrices.translate(1f, 0.99f / scale, -1f)
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

        fun <T> create(): TargetRenderer<T> where T : Skill, T : TargetRenderTrigger = object : TargetRenderer<T> {}
    }
}