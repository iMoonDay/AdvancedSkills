package com.imoonday.advskills_re.client.render.entity

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.model.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.util.math.*
import net.minecraft.util.*

class MeteoriteEntityRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<MeteoriteEntity>(ctx) {

    private val main: ModelPart = ctx.getPart(ClientRegistry.METEORITE_MODEL_LAYER)

    override fun render(
        entity: MeteoriteEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        matrices.push()
        val scale: Float = (entity.radius + 0.5f) * 2f
        matrices.scale(scale, scale, scale)
        matrices.translate(0f, -(0.75f + (entity.radius - 0.5f) / 2f / scale), 0f)
        val consumer = vertexConsumers.getBuffer(LAYER)
        main.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f)
        matrices.pop()
    }

    override fun getTexture(entity: MeteoriteEntity): Identifier = TEXTURE

    companion object {

        private val TEXTURE: Identifier = id("textures/entity/meteorite.png")
        private val LAYER: RenderLayer = RenderLayer.getEntityCutoutNoCull(TEXTURE)
        val texturedModelData: TexturedModelData
            get() {
                val modelData = ModelData()
                val modelPartData = modelData.root
                modelPartData.addChild(
                    "main",
                    ModelPartBuilder.create().uv(86, 32)
                        .cuboid(-4.0f, -16.0f, -4.0f, 8.0f, 16.0f, 8.0f, Dilation(0.0f)).uv(50, 47)
                        .cuboid(-5.0f, -15.0f, -5.0f, 10.0f, 14.0f, 10.0f, Dilation(0.0f)).uv(0, 40)
                        .cuboid(-6.0f, -14.0f, -6.0f, 12.0f, 12.0f, 12.0f, Dilation(0.0f)).uv(0, 0)
                        .cuboid(-8.0f, -12.0f, -4.0f, 16.0f, 8.0f, 8.0f, Dilation(0.0f)).uv(0, 16)
                        .cuboid(-7.0f, -13.0f, -5.0f, 14.0f, 10.0f, 10.0f, Dilation(0.0f)).uv(0, 64)
                        .cuboid(-5.0f, -13.0f, -7.0f, 10.0f, 10.0f, 14.0f, Dilation(0.0f)).uv(48, 74)
                        .cuboid(-4.0f, -12.0f, -8.0f, 8.0f, 8.0f, 16.0f, Dilation(0.0f)),
                    ModelTransform.pivot(0.0f, 24.0f, 0.0f)
                )
                return TexturedModelData.of(modelData, 128, 128)
            }
    }
}