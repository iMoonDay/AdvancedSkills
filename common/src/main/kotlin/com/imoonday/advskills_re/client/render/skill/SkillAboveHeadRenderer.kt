package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.math.*

interface SkillAboveHeadRenderer<T> : IPlayerFeatureRenderer<T> where T : Skill, T : UsingRenderTrigger {

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

        matrices.push()
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
        matrices.translate(-0.5, 0.65, -0.5)
        val model = context.modelManager.getModel(skill.getRenderModel(player, clientPlayer))
        context.itemRenderer.renderBakedItemQuads(
            matrices,
            provider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()),
            model.getQuads(null, null, player.random),
            ItemStack.EMPTY,
            0xF000F0,
            OverlayTexture.DEFAULT_UV
        )
        matrices.pop()
    }

    companion object {

        fun <T> create(): SkillAboveHeadRenderer<T> where T : Skill, T : UsingRenderTrigger =
            object : SkillAboveHeadRenderer<T> {}
    }
}