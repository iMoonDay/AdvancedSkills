package com.imoonday.entity.render.feature

import com.imoonday.init.ModSkills
import com.imoonday.init.isConfined
import com.imoonday.init.isDisarmed
import com.imoonday.init.isSilenced
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis

/**
 * from Twilight Forest
 */
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
        for (c in 0 until count) {
            stack.push()

            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateAngleY * (180f / Math.PI.toFloat()) + (c * (360f / count))))

            val scale = (entity.width * 1.2f).coerceAtMost(1.0f)

            stack.translate(-0.5, (entity.height - scale) * 0.5, -0.5)

            stack.translate(0f, 0f, (entity.width).coerceAtLeast(0.75f) + horizonOffset)

            stack.scale(scale, scale, scale)

            val model = context.modelManager.getModel(modelIdentifier)
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
        val silenceModelId = ModelIdentifier(Registries.ITEM.getId(ModSkills.PRIMARY_SILENCE.item), "inventory")
        val disarmModelId = ModelIdentifier(Registries.ITEM.getId(ModSkills.DISARM.item), "inventory")
        val confinementModelId = ModelIdentifier(Registries.ITEM.getId(Items.BARRIER), "inventory")
    }
}