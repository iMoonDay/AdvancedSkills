package com.imoonday.entity.render.feature

import com.imoonday.trigger.FeatureRendererTrigger
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity

class SkillLayer<T : PlayerEntity, M : EntityModel<T>>(
    private val renderer: FeatureRendererContext<T, M>,
    private val context: EntityRendererFactory.Context,
    val trigger: FeatureRendererTrigger,
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
        if (trigger.shouldRender(entity)) {
            trigger.render(
                stack,
                vertexConsumers,
                light,
                entity,
                limbAngle,
                limbDistance,
                tickDelta,
                animationProgress,
                headYaw,
                headPitch,
                renderer,
                context
            )
        }
    }
}
