package com.imoonday.entity.render

import com.imoonday.entity.HookEntity
import com.imoonday.util.toBlockPos
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.LightType
import org.joml.Matrix4f
import kotlin.math.cos
import kotlin.math.sin

class HookEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<HookEntity>(context) {

    override fun render(
        entity: HookEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val owner = entity.owner ?: return
        renderHook(entity, owner, matrices, tickDelta, vertexConsumers)
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    private fun renderHook(
        target: Entity,
        owner: Entity,
        matrices: MatrixStack,
        tickDelta: Float,
        provider: VertexConsumerProvider,
    ) {
        matrices.push()
        val pos = owner.getLeashPos(tickDelta)
        val rotationAngle = ((if (target is LivingEntity) (MathHelper.lerp(
            tickDelta,
            target.prevBodyYaw,
            target.bodyYaw
        )) else target.bodyYaw) * (Math.PI.toFloat() / 180)).toDouble() + 1.5707963267948966
        val leashOffset = target.getLeashOffset(tickDelta)
        val entityPosX = cos(rotationAngle) * leashOffset.z + sin(rotationAngle) * leashOffset.x
        val entityPosY = sin(rotationAngle) * leashOffset.z - cos(rotationAngle) * leashOffset.x
        val entityPosZ = MathHelper.lerp(tickDelta.toDouble(), target.prevX, target.x) + entityPosX
        val entityPosYAdjusted = MathHelper.lerp(tickDelta.toDouble(), target.prevY, target.y) + leashOffset.y
        val entityPosZAdjusted = MathHelper.lerp(tickDelta.toDouble(), target.prevZ, target.z) + entityPosY

        matrices.translate(entityPosX, leashOffset.y, entityPosY)
        val leashLengthX = (pos.x - entityPosZ).toFloat()
        val leashLengthY = (pos.y - entityPosYAdjusted).toFloat()
        val leashLengthZ = (pos.z - entityPosZAdjusted).toFloat()
        val leashThickness = 0.025f
        val vertexConsumer = provider.getBuffer(RenderLayer.getLeash())
        val positionMatrix = matrices.peek().positionMatrix
        val inverseSqrt =
            MathHelper.inverseSqrt(leashLengthX * leashLengthX + leashLengthZ * leashLengthZ) * leashThickness / 2.0f
        val offsetX = leashLengthZ * inverseSqrt
        val offsetZ = leashLengthX * inverseSqrt
        val entityBlockPos = BlockPos.ofFloored(target.getCameraPosVec(tickDelta))
        val holdingEntityBlockPos = pos.toBlockPos()
        val entityBlockLight = target.world.getLightLevel(LightType.BLOCK, entityBlockPos)
        val holdingEntityBlockLight =
            target.world.getLightLevel(LightType.BLOCK, holdingEntityBlockPos)
        val entitySkyLight = target.world.getLightLevel(LightType.SKY, entityBlockPos)
        val holdingEntitySkyLight = target.world.getLightLevel(LightType.SKY, holdingEntityBlockPos)
        var leashSegment = 0
        while (leashSegment <= 24) {
            renderLeashPiece(
                vertexConsumer,
                positionMatrix,
                leashLengthX,
                leashLengthY,
                leashLengthZ,
                entityBlockLight,
                holdingEntityBlockLight,
                entitySkyLight,
                holdingEntitySkyLight,
                leashThickness,
                leashThickness,
                offsetX,
                offsetZ,
                leashSegment
            )
            ++leashSegment
        }

        leashSegment = 24
        while (leashSegment >= 0) {
            renderLeashPiece(
                vertexConsumer,
                positionMatrix,
                leashLengthX,
                leashLengthY,
                leashLengthZ,
                entityBlockLight,
                holdingEntityBlockLight,
                entitySkyLight,
                holdingEntitySkyLight,
                leashThickness,
                0.0f,
                offsetX,
                offsetZ,
                leashSegment
            )
            --leashSegment
        }

        matrices.pop()
    }

    private fun renderLeashPiece(
        vertexConsumer: VertexConsumer,
        positionMatrix: Matrix4f,
        f: Float,
        g: Float,
        h: Float,
        leashedEntityBlockLight: Int,
        holdingEntityBlockLight: Int,
        leashedEntitySkyLight: Int,
        holdingEntitySkyLight: Int,
        i: Float,
        j: Float,
        k: Float,
        l: Float,
        pieceIndex: Int,
    ) {
        val m = pieceIndex.toFloat() / 24.0f
        val n = MathHelper.lerp(m, leashedEntityBlockLight.toFloat(), holdingEntityBlockLight.toFloat()).toInt()
        val o = MathHelper.lerp(m, leashedEntitySkyLight.toFloat(), holdingEntitySkyLight.toFloat()).toInt()
        val p = LightmapTextureManager.pack(n, o)
        val r = 0.75f * 0.7f
        val s = 0.75f * 0.7f
        val t = 0.75f * 0.7f
        val u = f * m
        val v = if (g > 0.0f) g * m * m else g - g * (1.0f - m) * (1.0f - m)
        val w = h * m
        vertexConsumer.vertex(positionMatrix, u - k, v + j, w + l).color(r, s, t, 1.0f).light(p).next()
        vertexConsumer.vertex(positionMatrix, u + k, v + i - j, w - l).color(r, s, t, 1.0f).light(p).next()
    }

    override fun getTexture(entity: HookEntity): Identifier = texture

    companion object {

        private val texture = Identifier("textures/entity/lead_knot.png")
    }
}