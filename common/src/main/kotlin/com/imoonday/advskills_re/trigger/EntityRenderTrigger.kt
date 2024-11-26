package com.imoonday.advskills_re.trigger

import net.minecraft.client.render.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface EntityRenderTrigger : SkillTrigger {

    fun shouldRender(player: PlayerEntity, entity: Entity): Boolean

    fun render(
        camera: Camera,
        entity: Entity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) = Unit
}