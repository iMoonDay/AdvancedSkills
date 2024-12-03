package com.imoonday.advskills_re.client.render.skill.renderer

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*
import net.minecraft.util.math.*
import org.joml.*

class InsightfulEyeSkillRenderer : IPlayerFeatureRenderer<InsightfulEyeSkill> {

    override fun <E : PlayerEntity, M : EntityModel<E>> render(
        skill: InsightfulEyeSkill,
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        player: E,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<E, M>,
        context: EntityRendererFactory.Context
    ) {
        val clientPlayer = clientPlayer ?: return
        if (!skill.shouldRenderFeature(player, clientPlayer)) return
        val cameraPos = context.renderDispatcher.camera.pos
        if (!player.shouldRender(cameraPos.x, cameraPos.y, cameraPos.z)) return
        if (player.isInvisibleTo(player)) return
        player.equippedSkills.forEachIndexed { index, equippedSkill ->
            matrices.push()
            matrices.scale(0.5f, 0.5f, 0.5f)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f))
            matrices.translate(
                index - player.equippedSkills.size / 2f + 0.5f,
                if (player.shouldRenderName() && !player.isSneaky) player.nameLabelHeight else player.height,
                0.0f
            )
            val entry: MatrixStack.Entry = matrices.peek()
            val matrix4f = entry.positionMatrix
            val matrix3f = entry.normalMatrix
            val vertexConsumer: VertexConsumer = provider.getBuffer(
                RenderLayer.getEntityCutoutNoCull(equippedSkill.icon)
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
}