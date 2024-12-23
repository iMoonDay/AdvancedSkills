package com.imoonday.advskills_re.client.render.entity.feature

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.init.*
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
        delta -= 45
        if (entity.isDisarmed) {
            renderEffects(matrices, vertexConsumers, entity, delta, disarmModelId, horizonOffset, 4)
            horizonOffset += 0.5f
        }
        delta -= 45
        if (entity.isConfined) {
            renderEffects(matrices, vertexConsumers, entity, delta, confinementModelId, horizonOffset, 4)
            horizonOffset += 0.5f
        }
        delta -= 45
        if (entity.isVulnerable) {
            renderEffects(
                matrices,
                vertexConsumers,
                entity,
                delta,
                vulnerableModelId,
                horizonOffset,
                entity.vulnerableLevel.coerceAtMost((4 + horizonOffset * 4).toInt())
            )
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
        val rotateAngleY = (entity.age + tickDelta) / 20.0f

        val model = context.modelManager.getModel(modelIdentifier)
        for (c in 0 until count) {
            stack.push()

            stack.translate(-0.5, 0.5, -0.5)
            stack.scale(-1f, -1f, 1f)
            stack.translate(-0.5, -0.5, 0.5)
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateAngleY * (180f / Math.PI.toFloat()) + (c * (360f / count))))
            stack.translate(-0.5, -0.5, 0.0)
            stack.translate(0f, 0f, 0.5f + horizonOffset)

            for (dir in directions) {
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
    }

    companion object {

        private val directions = Direction.entries

        val silenceModelId = Skills.PRIMARY_SILENCE.modelId
        val disarmModelId = Skills.DISARM.modelId
        val confinementModelId = ModelIdentifier(Registries.ITEM.getId(Items.BARRIER), "inventory")
        val vulnerableModelId = Skills.ARMOR_SHATTERER.modelId
    }
}