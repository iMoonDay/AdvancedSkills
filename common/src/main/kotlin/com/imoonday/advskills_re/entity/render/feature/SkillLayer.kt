package com.imoonday.advskills_re.entity.render.feature

import com.imoonday.advskills_re.trigger.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*

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
