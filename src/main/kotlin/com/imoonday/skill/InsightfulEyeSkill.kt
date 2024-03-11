package com.imoonday.skill

import com.imoonday.component.equippedSkills
import com.imoonday.trigger.FeatureRendererTrigger
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix3f
import org.joml.Matrix4f

class InsightfulEyeSkill : PassiveSkill(
    id = "insightful_eye",
    rarity = Rarity.EPIC
), FeatureRendererTrigger {

    override fun <T : PlayerEntity, M : EntityModel<T>> render(
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        player: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<T, M>,
        context: EntityRendererFactory.Context,
    ) {
        if (player !is OtherClientPlayerEntity) return
        val cameraPos = context.renderDispatcher.camera.pos
        if (!player.shouldRender(cameraPos.x, cameraPos.y, cameraPos.z)) return
        if (player.isInvisibleTo(MinecraftClient.getInstance().player)) return
        player.equippedSkills.forEachIndexed { index, skill ->
            matrices.push()
            matrices.scale(0.5f, 0.5f, 0.5f)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f))
            matrices.translate(
                index - 1.5f,
                if (player.shouldRenderName() && !player.isSneaky) player.nameLabelHeight else player.height,
                0.0f
            )
            val entry: MatrixStack.Entry = matrices.peek()
            val matrix4f = entry.positionMatrix
            val matrix3f = entry.normalMatrix
            val vertexConsumer: VertexConsumer = provider.getBuffer(
                RenderLayer.getEntityCutoutNoCull(skill.icon)
            )
            produceVertex(vertexConsumer, matrix4f, matrix3f, light, 0.0f, 0, 0, 1)
            produceVertex(vertexConsumer, matrix4f, matrix3f, light, 1.0f, 0, 1, 1)
            produceVertex(vertexConsumer, matrix4f, matrix3f, light, 1.0f, 1, 1, 0)
            produceVertex(vertexConsumer, matrix4f, matrix3f, light, 0.0f, 1, 0, 0)
            matrices.pop()
        }
    }

    private fun produceVertex(
        vertexConsumer: VertexConsumer,
        positionMatrix: Matrix4f,
        normalMatrix: Matrix3f,
        light: Int,
        x: Float,
        y: Int,
        textureU: Int,
        textureV: Int,
    ) {
        vertexConsumer.vertex(positionMatrix, x - 0.5f, y.toFloat() - 0.25f, 0.0f)
            .color(255, 255, 255, 255)
            .texture(textureU.toFloat(), textureV.toFloat())
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(normalMatrix, 0.0f, 1.0f, 0.0f)
            .next()
    }

    override fun shouldRender(player: PlayerEntity): Boolean =
        MinecraftClient.getInstance()?.player?.equippedSkills?.contains(this) == true && player is OtherClientPlayerEntity
}