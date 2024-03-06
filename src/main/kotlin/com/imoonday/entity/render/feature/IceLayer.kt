package com.imoonday.entity.render.feature

import com.imoonday.init.isForceFrozen
import net.minecraft.block.Blocks
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.RotationAxis

class IceLayer<T : LivingEntity, M : EntityModel<T>>(
    renderer: FeatureRendererContext<T, M>,
    private val context: EntityRendererFactory.Context,
) : FeatureRenderer<T, M>(renderer) {

    override fun render(
        stack: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        entity: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
    ) {
        if (!entity.isForceFrozen) return

        stack.pop()
        stack.push()
        stack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(entity.yaw))
        stack.translate(-0.5 * entity.width * 1.2, -(entity.height * 0.2) / 2.0, -0.5 * entity.width * 1.2)
        stack.scale(entity.width * 1.2f, entity.height * 1.2f, entity.width * 1.2f)
        context.blockRenderManager.renderBlockAsEntity(
            Blocks.ICE.defaultState,
            stack,
            vertexConsumers,
            light,
            OverlayTexture.DEFAULT_UV
        )
        stack.pop()
        stack.push()
    }
}
