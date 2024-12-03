package com.imoonday.advskills_re.client.render.entity

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.model.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.util.math.*
import net.minecraft.util.*
import net.minecraft.util.math.*

class MagnetEntityRenderer(val context: EntityRendererFactory.Context) :
    EntityRenderer<MagnetEntity>(context) {

    private val main: ModelPart = context.getPart(ClientRegistry.MAGNET_MODEL_LAYER)

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