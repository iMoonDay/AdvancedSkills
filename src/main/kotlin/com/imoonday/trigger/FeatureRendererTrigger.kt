package com.imoonday.trigger

import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import kotlin.math.cos
import kotlin.math.sin

interface FeatureRendererTrigger : SkillTrigger {

    fun <T : PlayerEntity, M : EntityModel<T>> render(
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
    }

    fun shouldRender(player: PlayerEntity): Boolean = player.isUsing()

    fun <T : PlayerEntity> renderSkillAboveHead(
        matrices: MatrixStack,
        context: EntityRendererFactory.Context,
        provider: VertexConsumerProvider,
        player: T,
    ) {
        matrices.push()
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
        matrices.translate(-0.5, 0.65, -0.5)
        val model = context.modelManager.getModel(asSkill().modelIdentifier)
        context.itemRenderer.renderBakedItemQuads(
            matrices,
            provider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()),
            model.getQuads(null, null, player.random),
            ItemStack.EMPTY,
            0xF000F0,
            OverlayTexture.DEFAULT_UV
        )
        matrices.pop()
    }

    fun <T : PlayerEntity> renderSkillAround(
        player: T,
        tickDelta: Float,
        matrices: MatrixStack,
        context: EntityRendererFactory.Context,
        provider: VertexConsumerProvider,
    ) {
        val age: Float = player.age + tickDelta
        val rotateAngleY = age / -20.0f
        val rotateAngleX: Float = sin(age / 5.0f) / 4.0f
        val rotateAngleZ: Float = cos(age / 5.0f) / 4.0f

        for (c in 0 until 4) {
            matrices.push()

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180 + rotateAngleZ * (180f / Math.PI.toFloat())))
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateAngleY * (180f / Math.PI.toFloat()) + (c * (360f / 4))))
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateAngleX * (180f / Math.PI.toFloat())))
            matrices.translate(-0.5, -0.65, -0.5)

            matrices.translate(0f, 0f, -0.75f)

            val model: BakedModel = context.modelManager.getModel(asSkill().modelIdentifier)
            for (dir in Direction.entries) {
                context.itemRenderer.renderBakedItemQuads(
                    matrices,
                    provider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()),
                    model.getQuads(null, dir, player.random).ifEmpty {
                        model.getQuads(null, null, player.random)
                    },
                    ItemStack.EMPTY,
                    0xF000F0,
                    OverlayTexture.DEFAULT_UV
                )
            }
            matrices.pop()
        }
    }
}