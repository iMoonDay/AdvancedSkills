package com.imoonday.entity.render

import com.imoonday.entity.MagnetEntity
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

class MagnetEntityRenderer(val context: EntityRendererFactory.Context) :
    EntityRenderer<MagnetEntity>(context) {

    private val main: ModelPart = context.getPart(ModEntities.MAGNET_MODEL_LAYER)

    override fun render(
        entity: MagnetEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val rotation = entity.age * 5 + tickDelta
        matrices.push()
        matrices.translate(0f, 1.55f, 0f)
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(rotation))
        main.render(matrices, vertexConsumers.getBuffer(layer), light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f)
        matrices.pop()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    override fun getTexture(entity: MagnetEntity): Identifier = texture

    companion object {

        val texture = id("textures/entity/magnet.png")
        private val layer: RenderLayer = RenderLayer.getEntityCutoutNoCull(texture)
    }
}