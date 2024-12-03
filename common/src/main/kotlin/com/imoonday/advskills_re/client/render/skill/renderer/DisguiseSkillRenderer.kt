package com.imoonday.advskills_re.client.render.skill.renderer

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.client.network.*
import net.minecraft.client.render.*
import net.minecraft.client.render.block.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*
import net.minecraft.util.math.*
import net.minecraft.world.*

class DisguiseSkillRenderer : IPlayerEntityRenderer<DisguiseSkill> {

    override fun render(
        skill: DisguiseSkill,
        player: AbstractClientPlayerEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ): Boolean {
        if (!skill.isDisguising(player)) return false
        val pos = skill.getDisguisePos(player) ?: return false
        return renderBlockUnderPlayer(player, pos, matrices, vertexConsumers, light, tickDelta)
    }

    private fun renderBlockUnderPlayer(
        player: PlayerEntity,
        pos: BlockPos,
        matrixStack: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        tickDelta: Float
    ): Boolean {
        matrixStack.push()
        val offset = Vec3d.of(player.blockPos) - Vec3d(player.prevX, player.prevY, player.prevZ).lerp(
            player.pos,
            tickDelta.toDouble()
        )
        matrixStack.translate(offset.x, offset.y, offset.z)
        val world = player.world
        return client?.blockRenderManager
            ?.renderBlock(
                world,
                pos,
                world.getBlockState(pos),
                matrixStack,
                provider,
                light,
                OverlayTexture.DEFAULT_UV
            )
            .also { matrixStack.pop() } ?: false
    }

    private fun BlockRenderManager.renderBlock(
        world: World,
        pos: BlockPos,
        state: BlockState,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ): Boolean {
        this as BlockRenderManagerAccessor
        return when (state.renderType) {
            BlockRenderType.MODEL -> {
                val bakedModel = this.getModel(state)
                val i: Int = this.blockColors.getColor(state, world, pos, 0)
                val f = (i shr 16 and 0xFF).toFloat() / 255.0f
                val g = (i shr 8 and 0xFF).toFloat() / 255.0f
                val h = (i and 0xFF).toFloat() / 255.0f
                this.blockModelRenderer
                    .render(
                        matrices.peek(),
                        vertexConsumers.getBuffer(RenderLayers.getEntityBlockLayer(state, false)),
                        state,
                        bakedModel,
                        f,
                        g,
                        h,
                        light,
                        overlay
                    )
                true
            }

            BlockRenderType.ENTITYBLOCK_ANIMATED -> {
                client!!.blockEntityRenderDispatcher.renderEntity(
                    world.getBlockEntity(pos),
                    matrices,
                    vertexConsumers,
                    light,
                    overlay
                )
                true
            }

            else -> false
        }
    }
}