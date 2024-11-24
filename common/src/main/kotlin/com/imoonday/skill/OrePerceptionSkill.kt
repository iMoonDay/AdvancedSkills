package com.imoonday.skill

import com.imoonday.advanced_skills_re.api.*
import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.block.*
import net.minecraft.client.util.math.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import java.awt.*
import java.util.function.*

class OrePerceptionSkill : Skill(
    id = "ore_perception",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, WorldRendererTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override val persistTime: Int = 20 * 5

    override fun renderLast(context: WorldRenderContext) {
        super.renderLast(context)
        if (clientPlayer?.isUsing() == true) {
            val world = clientPlayer!!.world
            val stack = context.matrixStack()
            clientPlayer!!.boundingBox.expand(15.0)
                .blockPosSet
                .asSequence()
                .filter {
                    val state = world.getBlockState(it)
                    !state.isAir && (state.isTagMatches("ores") || state.isTagMatches { it.id.path.endsWith("_ores") })
                }.forEach { highlightBlock(stack, world, it) }
        }
    }

    private fun highlightBlock(
        stack: MatrixStack,
        world: World,
        pos: BlockPos
    ) {
        val state = world.getBlockState(pos)
        val shape = state.getOutlineShape(world, pos).simplify()
        if (shape.isEmpty) return
        val block = state.block
        val color = when {
            block is Colorful -> block.getColor(world, state, pos)
            state.isIn(BlockTags.COAL_ORES) -> Color.BLACK
            state.isIn(BlockTags.IRON_ORES) -> Color.GRAY
            state.isIn(BlockTags.GOLD_ORES) -> Color.YELLOW
            state.isIn(BlockTags.DIAMOND_ORES) -> Color.CYAN
            state.isIn(BlockTags.LAPIS_ORES) -> Color.BLUE
            state.isIn(BlockTags.EMERALD_ORES) -> Color.GREEN
            state.isIn(BlockTags.REDSTONE_ORES) -> Color.RED
            state.isIn(BlockTags.COPPER_ORES) -> Color.ORANGE
            state.isTagMatches("quartz_ores") -> Color.PINK
            else -> Color.WHITE
        }
        val box = shape.boundingBox
        Renderer3d.renderThroughWalls()
        Renderer3d.renderEdged(
            stack,
            color.alpha(0.25),
            color,
            Vec3d(box.minX + pos.x, box.minY + pos.y, box.minZ + pos.z),
            Vec3d(box.xLength, box.yLength, box.zLength)
        )
        Renderer3d.stopRenderThroughWalls()
    }

    fun BlockState.isTagMatches(predicate: Predicate<TagKey<Block>>) = streamTags().anyMatch(predicate)

    fun BlockState.isTagMatches(id: String) = isTagMatches { it.id.path == id }

    fun interface Colorful {

        fun getColor(world: World, state: BlockState, pos: BlockPos): Color
    }
}