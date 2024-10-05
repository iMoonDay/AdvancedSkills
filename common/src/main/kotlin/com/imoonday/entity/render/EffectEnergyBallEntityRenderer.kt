package com.imoonday.entity.render

import com.imoonday.entity.EffectEnergyBallEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix3f
import org.joml.Matrix4f

abstract class EffectEnergyBallEntityRenderer<T : EffectEnergyBallEntity>(context: EntityRendererFactory.Context) :
    EntityRenderer<T>(context) {

    abstract val texture: Identifier
    val layer: RenderLayer
        get() = RenderLayer.getEntityCutoutNoCull(texture)

    protected open var scale = 2.0f

    override fun render(
        entity: T,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
    ) {
        matrixStack.push()
        matrixStack.scale(scale, scale, scale)
        matrixStack.multiply(dispatcher.rotation)
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f))
        val entry = matrixStack.peek()
        val matrix4f = entry.positionMatrix
        val matrix3f = entry.normalMatrix
        val vertexConsumer = vertexConsumerProvider.getBuffer(layer)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 0, 0, 1)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 0, 1, 1)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 1, 1, 0)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 1, 0, 0)
        matrixStack.pop()
        super.render(entity, f, g, matrixStack, vertexConsumerProvider, i)
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
        vertexConsumer.vertex(positionMatrix, x - 0.5f, y.toFloat() - 0.25f, 0.0f).color(255, 255, 255, 255)
            .texture(textureU.toFloat(), textureV.toFloat()).overlay(OverlayTexture.DEFAULT_UV).light(light)
            .normal(normalMatrix, 0.0f, 1.0f, 0.0f).next()
    }

    override fun getTexture(entity: T): Identifier = texture
}