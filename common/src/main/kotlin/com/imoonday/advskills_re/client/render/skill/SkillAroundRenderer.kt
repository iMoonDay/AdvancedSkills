package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import net.minecraft.client.render.*
import net.minecraft.client.render.model.*
import net.minecraft.client.util.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.math.*
import kotlin.math.*

interface SkillAroundRenderer<T> : IPostLivingEntityRenderer<T> where T : Skill, T : RenderPostLivingTrigger {

    override fun render(
        skill: T,
        entity: LivingEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val client = client ?: return
        val player = entity as? PlayerEntity ?: return
        val clientPlayer = clientPlayer ?: return
        if (!skill.shouldRenderPostLiving(player, clientPlayer)) return
        if (player.isInvisible || player.isInvisibleTo(clientPlayer)) return

        val age: Float = player.age + tickDelta
        val rotateAngleY = age / -20.0f
        val rotateAngleX: Float = sin(age / 5.0f) / 4.0f
        val rotateAngleZ: Float = cos(age / 5.0f) / 4.0f

        for (c in 0 until 4) {
            matrices.push()

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180 + rotateAngleZ * (180f / Math.PI.toFloat())))
            matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(rotateAngleY * (180f / Math.PI.toFloat()) + (c * (360f / 4)))
            )
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateAngleX * (180f / Math.PI.toFloat())))
            matrices.translate(-0.5, -0.65, -0.5)

            matrices.translate(0f, 0f, -0.75f)
            val model: BakedModel = client.bakedModelManager.getModel(getRenderModel(skill))
            for (dir in Direction.entries) {
                client.itemRenderer.renderBakedItemQuads(
                    matrices,
                    vertexConsumers.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()),
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

    fun getRenderModel(skill: T): ModelIdentifier = skill.modelId

    companion object {

        fun <T> create(model: ModelIdentifier? = null): SkillAroundRenderer<T> where T : Skill, T : RenderPostLivingTrigger =
            object : SkillAroundRenderer<T> {

                override fun getRenderModel(skill: T): ModelIdentifier = model ?: super.getRenderModel(skill)
            }
    }
}