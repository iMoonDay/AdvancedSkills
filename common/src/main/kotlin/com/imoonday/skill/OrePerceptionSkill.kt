package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.WorldRendererTrigger
import com.imoonday.util.*
import me.x150.renderer.render.Renderer3d
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.minecraft.block.BlockState
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.awt.Color

class OrePerceptionSkill : Skill(
    id = "ore_perception",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, WorldRendererTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun getPersistTime(): Int = 20 * 5

    override fun renderLast(context: WorldRenderContext) {
        super.renderLast(context)
        if (clientPlayer?.isUsing() == true) {
            val world = clientPlayer!!.world
            clientPlayer!!.boundingBox.expand(15.0)
                .blockPosSet
                .asSequence()
                .filter { pos ->
                    world.getBlockState(pos).isIn(ConventionalBlockTags.ORES)
                }.forEach {
                    val state = world.getBlockState(it)
                    val shape = state.getOutlineShape(world, it).simplify()
                    if (shape.isEmpty) return
                    val block = state.block
                    val color = when {
                        block is Colorful -> block.getColor(world, state, it)
                        state.isIn(BlockTags.COAL_ORES) -> Color.BLACK
                        state.isIn(BlockTags.IRON_ORES) -> Color.GRAY
                        state.isIn(BlockTags.GOLD_ORES) -> Color.YELLOW
                        state.isIn(BlockTags.DIAMOND_ORES) -> Color.CYAN
                        state.isIn(BlockTags.LAPIS_ORES) -> Color.BLUE
                        state.isIn(BlockTags.EMERALD_ORES) -> Color.GREEN
                        state.isIn(BlockTags.REDSTONE_ORES) -> Color.RED
                        state.isIn(BlockTags.COPPER_ORES) -> Color.ORANGE
                        state.isIn(ConventionalBlockTags.QUARTZ_ORES) -> Color.PINK
                        else -> Color.WHITE
                    }
                    val box = shape.boundingBox
                    Renderer3d.renderThroughWalls()
                    Renderer3d.renderEdged(
                        context.matrixStack(),
                        color.alpha(0.25),
                        color,
                        Vec3d(box.minX + it.x, box.minY + it.y, box.minZ + it.z),
                        Vec3d(box.xLength, box.yLength, box.zLength)
                    )
                    Renderer3d.stopRenderThroughWalls()
                }
        }
    }

    fun interface Colorful {

        fun getColor(world: World, state: BlockState, pos: BlockPos): Color
    }
}