package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.render.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.math.*
import kotlin.math.*

interface SkillAroundRenderer<T> : IPlayerFeatureRenderer<T> where T : Skill, T : FeatureRendererTrigger {

    override fun <E : PlayerEntity, M : EntityModel<E>> render(
        skill: T,
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        player: E,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<E, M>,
        context: EntityRendererFactory.Context
    ) {
        val clientPlayer = clientPlayer ?: return
        if (!skill.shouldRenderFeature(player, clientPlayer)) return

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
            val model: BakedModel = context.modelManager.getModel(skill.getRenderModel(player, clientPlayer))
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

    companion object {

        fun <T> create(): SkillAroundRenderer<T> where T : Skill, T : UsingRenderTrigger =
            object : SkillAroundRenderer<T> {}
    }
}