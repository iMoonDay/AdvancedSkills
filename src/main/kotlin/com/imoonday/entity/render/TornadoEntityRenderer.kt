package com.imoonday.entity.render

import com.imoonday.entity.TornadoEntity
import com.imoonday.init.ModEntities
import com.imoonday.util.id
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis

class TornadoEntityRenderer(val context: EntityRendererFactory.Context) :
    EntityRenderer<TornadoEntity>(context) {

    private val main: ModelPart = context.getPart(ModEntities.TORNADO_MODEL_LAYER)

    override fun render(
        entity: TornadoEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val rotation = entity.age * 10 + tickDelta
        matrices.push()
        matrices.scale(2f, 2f, 2f)
        matrices.translate(0f, entity.height * 0.75f, 0f)
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(rotation))
        val vertexConsumer = vertexConsumers.getBuffer(layer)
        main.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f)
        matrices.pop()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    override fun getTexture(entity: TornadoEntity): Identifier = texture

    companion object {

        val texture = id("textures/entity/tornado.png")
        private val layer: RenderLayer = RenderLayer.getEntityTranslucentCull(texture)
    }
}