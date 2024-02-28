package com.imoonday.render

import com.imoonday.init.isForceFrozen
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis

object FrozenLayerRenderer {

    val ICE: SpriteIdentifier = SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier("block/ice"))

    fun renderFire(
        dispatcher: EntityRenderDispatcher,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        entity: Entity,
    ) {
        if (entity !is LivingEntity || !entity.isForceFrozen) return
        val sprite = ICE.sprite
        matrices.push()
        val f = entity.width * 1.4f
        matrices.scale(f, f, f)
        var g = 0.5f
        val h = 0.0f
        var i = entity.height / f
        var j = 0.0f
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-dispatcher.camera.yaw))
        matrices.translate(0.0f, 0.0f, -0.3f + i.toInt().toFloat() * 0.02f)
        var k = 0.0f
        var l = 0
        val vertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout())
        val entry = matrices.peek()
        while (i > 0.0f) {
            var m = sprite.minU
            val n = sprite.minV
            var o = sprite.maxU
            val p = sprite.maxV
            if (l / 2 % 2 == 0) {
                val q = o
                o = m
                m = q
            }
            drawFireVertex(entry, vertexConsumer, g - 0.0f, 0.0f - j, k, o, p)
            drawFireVertex(entry, vertexConsumer, -g - 0.0f, 0.0f - j, k, m, p)
            drawFireVertex(entry, vertexConsumer, -g - 0.0f, 1.4f - j, k, m, n)
            drawFireVertex(entry, vertexConsumer, g - 0.0f, 1.4f - j, k, o, n)
            i -= 0.45f
            j -= 0.45f
            g *= 0.9f
            k += 0.03f
            ++l
        }
        matrices.pop()
    }

    private fun drawFireVertex(
        entry: MatrixStack.Entry,
        vertices: VertexConsumer,
        x: Float,
        y: Float,
        z: Float,
        u: Float,
        v: Float,
    ) {
        vertices.vertex(entry.positionMatrix, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(0, 10)
            .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).normal(entry.normalMatrix, 0.0f, 1.0f, 0.0f)
            .next()
    }
}