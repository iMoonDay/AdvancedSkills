package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.skill.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*

interface IPlayerFeatureRenderer<T : Skill> : IRenderer<T> {

    fun <E : PlayerEntity, M : EntityModel<E>> render(
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
    )
}