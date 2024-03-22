package com.imoonday.skill

import com.imoonday.trigger.CrosshairTrigger
import com.imoonday.trigger.FeatureRendererTrigger
import com.imoonday.trigger.WorldRendererTrigger
import com.imoonday.util.*
import fi.dy.masa.malilib.util.NBTUtils
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.LightType
import org.joml.Matrix4f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GrapplingHookSkill : LongPressSkill(
    id = "grappling_hook",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 15,
    rarity = Rarity.EPIC
), FeatureRendererTrigger, WorldRendererTrigger, CrosshairTrigger {

    override fun getMaxPressTime(): Int = 20 * 3

    private val maxDistance = 30.0

    override fun onPress(player: ServerPlayerEntity): UseResult {
        val raycast = player.raycastVisualBlock(maxDistance)
        return if (raycast.type == HitResult.Type.BLOCK) {
            UseResult.startUsing(
                player,
                this,
                NBTUtils.writeVec3dToTag(raycast.pos, NbtCompound())
            ).withCooling(false)
        } else {
            UseResult.fail(failedMessage())
        }
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        return UseResult.success()
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        if (player.isUsing()) {
            player.fallDistance = 0f
            player.stopFallFlying()
            player.getUsingData()?.let {
                NBTUtils.readVec3d(it)?.run {
                    val pos = player.pos
                    val distance = distanceTo(pos)
                    if (player.blockPos.down() == toBlockPos()
                        || player.calculateAngle(this) > PI / 4.5
                    ) {
                        if (!player.world.isClient) {
                            player.stopUsing()
                            player.startCooling()
                        }
                        return@let
                    }
                    val rotation = player.rotationVector
                    val newVelocity = add(
                        rotation.x,
                        player.height.toDouble() / 2.0 + rotation.y,
                        rotation.z
                    ).subtract(pos).normalize()
                        .multiply((distance / maxDistance) + 1)
                    player.velocityDirty = true
                    player.addVelocity((newVelocity - player.velocity).multiply(0.5))
                }
            }
        }
        super.tick(player, usedTime)
    }

    override fun <T : PlayerEntity, M : EntityModel<T>> render(
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        player: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<T, M>,
        context: EntityRendererFactory.Context
    ) = renderHook(player, matrices, tickDelta, provider)

    private fun renderHook(
        player: PlayerEntity,
        matrices: MatrixStack,
        tickDelta: Float,
        provider: VertexConsumerProvider,
        thirdPerson: Boolean = true,
    ) {
        if (!player.isUsing() || player.getUsingData() == null) return
        val pos = NBTUtils.readVec3d(player.getUsingData())!!

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

    override fun renderAfterEntities(context: WorldRenderContext) {
        super.renderAfterEntities(context)
        if (context.camera().isThirdPerson || context.camera().focusedEntity != context.gameRenderer().client.player) return
        context.gameRenderer().client.player?.let {
            context.consumers()?.let { provider ->
                renderHook(
                    it,
                    context.matrixStack(),
                    context.tickDelta(),
                    provider,
                    false
                )
            }
        }
    }

    override fun getCrosshair(): Crosshair {
        clientPlayer?.run {
            if (isReady() && raycastVisualBlock(maxDistance).type == HitResult.Type.BLOCK) return Crosshairs.RING
        }
        return Crosshairs.NONE
    }
}