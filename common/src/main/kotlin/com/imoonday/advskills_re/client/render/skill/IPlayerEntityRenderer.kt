package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.skill.*
import net.minecraft.client.network.*
import net.minecraft.client.render.*
import net.minecraft.client.util.math.*

interface IPlayerEntityRenderer<T : Skill> : IRenderer<T> {

    fun render(
        skill: T,
        player: AbstractClientPlayerEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ): Boolean
}