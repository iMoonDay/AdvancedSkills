package com.imoonday.trigger

import com.imoonday.util.id
import com.mojang.blaze3d.systems.RenderSystem
import me.x150.renderer.render.Renderer2d
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.RotationAxis

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