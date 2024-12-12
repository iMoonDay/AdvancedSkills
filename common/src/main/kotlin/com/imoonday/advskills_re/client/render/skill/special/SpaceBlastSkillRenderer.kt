package com.imoonday.advskills_re.client.render.skill.special

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.util.math.*
import java.awt.*

class SpaceBlastSkillRenderer : IWorldRenderer<SpaceBlastSkill> {

    override fun renderAfterEntities(skill: SpaceBlastSkill, context: WorldRenderContext) {
        val player = clientPlayer ?: return
        if (!player.isUsing(skill)) return
        val stack = context.matrixStack()
        val progress = skill.getProgress(player)
        val white = Color.WHITE.alpha(25)
        val color =
            Color((progress).toFloat().coerceIn(0f, 1f), (1 - progress).toFloat().coerceIn(0f, 1f), 0f, 25 / 255f)
        val world = context.world()
        val breakableBlocks: MutableList<Renderer3d.BlockRenderInfo> = mutableListOf()
        val blocks: MutableList<Renderer3d.BlockRenderInfo> = mutableListOf()
        skill.forEachBlock(player) {
            val state = world.getBlockState(it)
            val start = Vec3d.of(it)
            val end = start + 1.0
            if (!state.isAir && state.getHardness(world, it) >= 0.0) {
                breakableBlocks.add(Renderer3d.BlockRenderInfo(it, start, end, color, color))
            } else {
                blocks.add(Renderer3d.BlockRenderInfo(it, start, end, white, white))
            }
        }
        Renderer3d.renderThroughWalls()
        Renderer3d.renderVisibleFaces(stack, breakableBlocks, fill = false)
        Renderer3d.renderVisibleFaces(stack, blocks, fill = false)
        Renderer3d.stopRenderThroughWalls()
    }
}