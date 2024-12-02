package com.imoonday.advskills_re.entity.render.feature

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.registry.*
import net.minecraft.util.math.*

class StatusEffectLayer<T : LivingEntity, M : EntityModel<T>>(
    renderer: FeatureRendererContext<T, M>,
    private val context: EntityRendererFactory.Context,
) : FeatureRenderer<T, M>(renderer) {

    override fun render(
        matrices: MatrixStack,
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
        var delta = tickDelta
        var horizonOffset = 0f
        if (entity.isSilenced) {
            renderEffects(matrices, vertexConsumers, entity, delta, silenceModelId, horizonOffset, 4)
            horizonOffset += 0.5f
        }
        delta -= 10
        if (entity.isDisarmed) {
            renderEffects(matrices, vertexConsumers, entity, delta, disarmModelId, horizonOffset, 4)
            horizonOffset += 0.5f
        }
        delta -= 10
        if (entity.isConfined) {
            renderEffects(matrices, vertexConsumers, entity, delta, confinementModelId, horizonOffset, 4)
            horizonOffset += 0.5f
        }
        delta -= 10
        if (entity.isWeakened) {
            renderEffects(matrices, vertexConsumers, entity, delta, weakenedModelId, horizonOffset, 4)
        }
    }

    private fun renderEffects(
        stack: MatrixStack,
        provider: VertexConsumerProvider,
        entity: T,
        tickDelta: Float,
        modelIdentifier: ModelIdentifier,
        horizonOffset: Float,
        count: Int,
    ) {
        val age: Float = entity.age + tickDelta
        val rotateAngleY = age / -20.0f

        stack.pop()
        val model = context.modelManager.getModel(modelIdentifier)
        val scale = (entity.width * 1.2f).coerceAtMost(1.0f)
        for (c in 0 until count) {
            stack.push()
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateAngleY * (180f / Math.PI.toFloat()) + (c * (360f / count))))
            stack.translate(-0.5, (entity.height - scale) * 0.5, -0.5)
            stack.translate(0f, 0f, (entity.width).coerceAtLeast(0.75f) + horizonOffset)
            stack.scale(scale, scale, scale)

            for (dir in Direction.entries) {
                context.itemRenderer.renderBakedItemQuads(
                    stack,
                    provider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()),
                    model.getQuads(null, dir, entity.random).ifEmpty {
                        model.getQuads(null, null, entity.random)
                    },
                    ItemStack.EMPTY,
                    0xF000F0,
                    OverlayTexture.DEFAULT_UV
                )
            }
            stack.pop()
        }
        stack.push()
    }

    companion object {

        val silenceModelId = Skills.PRIMARY_SILENCE.modelId
        val disarmModelId = Skills.DISARM.modelId
        val confinementModelId = ModelIdentifier(Registries.ITEM.getId(Items.BARRIER), "inventory")
        val weakenedModelId = Skills.ARMOR_SHATTERER.modelId
    }
}