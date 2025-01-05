package com.imoonday.advskills_re.client.render.entity.feature

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.init.*
import net.minecraft.client.render.*
import net.minecraft.client.util.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.registry.*
import net.minecraft.util.math.*

object StatusEffectRenderer {

    private val directions = Direction.entries

    val silenceModelId = Skills.PRIMARY_SILENCE.modelId
    val disarmModelId = Skills.DISARM.modelId
    val confinementModelId = ModelIdentifier(Registries.ITEM.getId(Items.BARRIER), "inventory")
    val vulnerableModelId = Skills.ARMOR_SHATTERER.modelId

    @JvmStatic
    fun render(
        entity: LivingEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
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
        entity: LivingEntity,
        tickDelta: Float,
        modelIdentifier: ModelIdentifier,
        horizonOffset: Float,
        count: Int,
    ) {
        val client1 = client ?: return

        val rotateAngleY = (entity.age + tickDelta) / 20.0f

        val model = client1.bakedModelManager.getModel(modelIdentifier)
        val scale = entity.width.coerceAtMost(1f)
        val width = entity.width / 2f
        val height = entity.height / 2f
        for (c in 0 until count) {
            stack.push()

            stack.translate(-0.5, 0.5, -0.5)
            stack.scale(-scale, -scale, scale)
            stack.translate(-0.5 / scale, -0.5 / scale, 0.5 / scale)
            stack.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                    rotateAngleY * (180f / Math.PI.toFloat()) + (c * (360f / count))
                )
            )
            stack.translate(-0.5, -0.5, 0.0)
            stack.translate(0f, height / scale, width / scale + horizonOffset)

            for (dir in directions) {
                client1.itemRenderer.renderBakedItemQuads(
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
}