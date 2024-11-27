package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.client.render.*
import net.minecraft.client.render.block.*
import net.minecraft.client.render.model.json.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.math.*
import net.minecraft.world.*

interface DisguiseTrigger : SkillTrigger {

    fun isDisguising(player: PlayerEntity): Boolean = player.isUsing()

    fun getDisguiseRenderer(player: PlayerEntity): DisguiseRenderer?

    fun interface DisguiseRenderer {

        fun render(
            matrixStack: MatrixStack,
            provider: VertexConsumerProvider,
            light: Int,
            tickDelta: Float
        ): Boolean
    }

    companion object {

        fun renderBlockUnderPlayer(
            player: PlayerEntity,
            pos: BlockPos,
            matrixStack: MatrixStack,
            provider: VertexConsumerProvider,
            light: Int,
            tickDelta: Float
        ): Boolean {
            matrixStack.push()
            val offset = Vec3d.of(player.blockPos) - player.pos
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

        fun BlockRenderManager.renderBlock(
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
                    this.builtinModelItemRenderer.render(
                        ItemStack(state.block),
                        ModelTransformationMode.NONE,
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
}