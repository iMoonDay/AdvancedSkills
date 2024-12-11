package com.imoonday.advskills_re.client.render.skill.special

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.entity.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.render.model.json.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*
import net.minecraft.util.math.*

class SwordSoulGuardingSkillRenderer : IPlayerFeatureRenderer<SwordSoulGuardingSkill> {

    override fun <E : PlayerEntity, M : EntityModel<E>> render(
        skill: SwordSoulGuardingSkill,
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
        if (player.isInvisible || player.isInvisibleTo(clientPlayer)) return

        matrices.push()
        matrices.translate(-0.2f, 0f, 0.5f)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - headYaw))
        matrices.multiply(
            RotationAxis.NEGATIVE_X.rotationDegrees(180f)
        )
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45f))
        matrices.scale(2f, 2f, 2f)
        context.itemRenderer.renderItem(
            EnchantedSwordEntityRenderer.sword,
            ModelTransformationMode.GROUND,
            context.renderDispatcher.getLight(player, tickDelta),
            OverlayTexture.DEFAULT_UV,
            matrices,
            provider,
            player.world,
            0
        )
        matrices.pop()
    }
}