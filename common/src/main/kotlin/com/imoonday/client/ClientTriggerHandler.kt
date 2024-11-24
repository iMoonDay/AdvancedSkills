package com.imoonday.client

import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.client.*
import net.minecraft.client.gui.hud.InGameHud.*
import net.minecraft.client.render.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*

object ClientTriggerHandler {

    fun isGlowing(entity: Entity): Boolean =
        clientPlayer?.getTriggers<GlowingTrigger>()
            ?.map { it.isGlowing(entity) }
            ?.any { it } ?: false

    fun worldRender(matrixStack: MatrixStack, tickDelta: Float, client: MinecraftClient) {
        client.player?.getTriggers<WorldRenderTrigger>()
            ?.forEach { it.apply(matrixStack, tickDelta, client) }
    }

    fun shouldInvertMouse(): Pair<Boolean, Boolean> =
        clientPlayer?.getTriggers<InvertMouseTrigger>()?.run {
            map { it.shouldInvertMouseX() }.any { it } to map { it.shouldInvertMouseY() }.any { it }
        } ?: (false to false)

    fun shouldInvertInput(): Pair<Boolean, Boolean> =
        clientPlayer?.getTriggers<InvertInputTrigger>()?.run {
            map { it.shouldInvertHorizontalInput() }.any { it } to map { it.shouldInvertVerticalInput() }.any { it }
        } ?: (false to false)

    fun getCameraMovement(original: Float): Float {
        var movement = original
        clientPlayer?.getTriggers<CameraUpdateMovementTrigger>()
            ?.forEach { movement = it.getDelta(movement) }
        return movement
    }

    fun getHeartType(player: PlayerEntity): HeartType? =
        player.getTriggers<HeartTypeTrigger>()
            .mapNotNull { it.getHeartType(player) }
            .maxByOrNull { it.second }?.first

    fun renderAfterEntity(
        client: MinecraftClient,
        camera: Camera,
        entity: Entity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        client.player?.run {
            getTriggers<EntityRenderTrigger>()
                .filter { it.shouldRender(this, entity) }
                .forEach { it.render(camera, entity, yaw, tickDelta, matrices, vertexConsumers, light) }
        }
    }
}