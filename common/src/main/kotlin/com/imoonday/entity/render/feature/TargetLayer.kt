package com.imoonday.entity.render.feature

import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*

class TargetLayer<T : LivingEntity, M : EntityModel<T>>(
    private val renderer: FeatureRendererContext<T, M>,
    private val context: EntityRendererFactory.Context,
    val trigger: TargetRenderTrigger,
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
        clientPlayer?.run {
            if (trigger.isTarget(this, entity)) {
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
}
