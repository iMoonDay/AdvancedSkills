package com.imoonday.advskills_re.client.render.skill.special

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.util.math.*
import java.awt.*
import java.util.concurrent.*

class OrePerceptionSkillRenderer : IWorldRenderer<OrePerceptionSkill> {

    override fun renderLast(skill: OrePerceptionSkill, context: WorldRenderContext) {
        val player = clientPlayer ?: return
        if (!player.isUsing(skill)) {
            if (!cleared) {
                oreCache.clear()
                cleared = true
            }
            return
        }
        if (oreCache.isEmpty()) return
        val world = player.world
        val stack = context.matrixStack()
        val frustum = context.frustum()
        val cameraPos = context.camera().pos
        Renderer3d.renderThroughWalls()
        val blocks = oreCache.entries
            .filter { entry ->
                val pos = entry.key
                world.isChunkLoaded(pos) && frustum?.isVisible(Box(pos)) == true
            }
            .map { entry ->
                val pos = entry.key
                createBlockRenderInfo(
                    pos,
                    entry.value.lighten(cameraPos.distanceTo(pos.toCenterPos()))
                )
            }
        Renderer3d.renderVisibleFaces(stack, blocks)
        Renderer3d.stopRenderThroughWalls()
    }

    private fun createBlockRenderInfo(
        pos: BlockPos,
        color: Color,
    ): Renderer3d.BlockRenderInfo {
        val start = Vec3d.of(pos)
        val end = start + 1.0
        return Renderer3d.BlockRenderInfo(
            pos, start, end, color.alpha(0.25), Color.WHITE.alpha(color.alpha)
        )
    }

    private fun Color.lighten(distance: Double): Color = alpha(MathHelper.clamp(distance / 16, 0.25, 1.0))

    companion object {

        private val oreCache: ConcurrentMap<BlockPos, Color> = ConcurrentHashMap()
        private var cleared = false

        fun updateOreCache(blocks: Map<BlockPos, Color>) {
            oreCache.clear()
            oreCache.putAll(blocks)
            cleared = false
        }
    }
}