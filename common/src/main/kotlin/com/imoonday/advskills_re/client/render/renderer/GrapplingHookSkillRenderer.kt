package com.imoonday.advskills_re.client.render.renderer

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import org.joml.*
import java.lang.Math
import kotlin.math.*

class GrapplingHookSkillRenderer : CrosshairRenderer<GrapplingHookSkill>(), IPlayerFeatureRenderer<GrapplingHookSkill>,
    IWorldRenderer<GrapplingHookSkill> {

    override fun <P : PlayerEntity, M : EntityModel<P>> render(
        skill: GrapplingHookSkill,
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        player: P,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<P, M>,
        context: EntityRendererFactory.Context
    ) = renderHook(skill, player, matrices, tickDelta, provider)

    private fun renderHook(
        skill: GrapplingHookSkill,
        player: PlayerEntity,
        matrices: MatrixStack,
        tickDelta: Float,
        provider: VertexConsumerProvider,
        thirdPerson: Boolean = true,
    ) {
        if (!player.isUsing(skill) || player.getUsingData(skill) == null) return
        val pos = NbtUtils.readVec3d(player.getUsingData(skill)) ?: return

        if (thirdPerson) matrices.pop()
        matrices.push()
        val rotationAngle = (MathHelper.lerp(
            tickDelta,
            player.prevBodyYaw,
            player.bodyYaw
        ) * (Math.PI.toFloat() / 180)).toDouble() + 1.5707963267948966
        val leashOffset = player.getLeashPos(1.0f) - player.pos
        val entityPosX = cos(rotationAngle) * leashOffset.z + sin(rotationAngle) * leashOffset.x
        val entityPosY = sin(rotationAngle) * leashOffset.z - cos(rotationAngle) * leashOffset.x
        val entityPosZ = MathHelper.lerp(tickDelta.toDouble(), player.prevX, player.x) + entityPosX
        val entityPosYAdjusted = MathHelper.lerp(tickDelta.toDouble(), player.prevY, player.y) + leashOffset.y
        val entityPosZAdjusted = MathHelper.lerp(tickDelta.toDouble(), player.prevZ, player.z) + entityPosY

        matrices.translate(entityPosX, if (thirdPerson) leashOffset.y else -0.5, entityPosY)
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
        val entityBlockPos = BlockPos.ofFloored(player.getCameraPosVec(tickDelta))
        val holdingEntityBlockPos = pos.toBlockPos()
        val entityBlockLight = player.world.getLightLevel(LightType.BLOCK, entityBlockPos)
        val holdingEntityBlockLight =
            player.world.getLightLevel(LightType.BLOCK, holdingEntityBlockPos)
        val entitySkyLight = player.world.getLightLevel(LightType.SKY, entityBlockPos)
        val holdingEntitySkyLight = player.world.getLightLevel(LightType.SKY, holdingEntityBlockPos)
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
        if (thirdPerson) matrices.push()
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

    override fun renderAfterEntities(skill: GrapplingHookSkill, context: WorldRenderContext) {
        val camera = context.camera()
        if (camera.isThirdPerson) return
        val player = context.gameRenderer().client.player ?: return
        if (camera.focusedEntity != player) return
        context.consumers()?.let {
            renderHook(
                skill,
                player,
                context.matrixStack(),
                context.tickDelta(),
                it,
                false
            )
        }
    }
}