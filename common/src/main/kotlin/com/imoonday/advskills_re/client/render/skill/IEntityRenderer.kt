package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.skill.*
import net.minecraft.client.render.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*

interface IEntityRenderer<T : Skill> : IRenderer<T> {

    fun render(
        skill: T,
        camera: Camera,
        entity: Entity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    )
}