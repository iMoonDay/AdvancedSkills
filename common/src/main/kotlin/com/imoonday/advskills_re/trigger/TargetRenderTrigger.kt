package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.util.math.*

interface TargetRenderTrigger : SkillTrigger {

    fun <T : LivingEntity, M : EntityModel<T>> render(
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        entity: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<T, M>,
        context: EntityRendererFactory.Context,
    ) = renderIndicator(matrices, context, provider, entity)

    fun renderIndicator(
        matrices: MatrixStack,
        context: EntityRendererFactory.Context,
        provider: VertexConsumerProvider,
        entity: Entity,
    ) {
        matrices.push()
        matrices.translate(1f, 1.5f, -1f)
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f))
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.setShaderTexture(0, id("indicator.png"))
        Renderer2d.renderTexture(matrices, 0.0, 0.0, 2.0, 2.0, 0f, 0f, 16.0, 16.0, 16.0, 16.0)
        RenderSystem.disableDepthTest()
        RenderSystem.disableBlend()
        matrices.pop()
    }

    fun isTarget(player: PlayerEntity, entity: LivingEntity): Boolean

    companion object {

        val modelId = ModelIdentifier(id("indicator"), "inventory")
    }
}