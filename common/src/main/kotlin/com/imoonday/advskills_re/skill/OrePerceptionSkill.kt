package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.player.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import java.awt.*
import java.util.concurrent.*
import java.util.function.*
import kotlin.math.*

class OrePerceptionSkill : Skill(
    id = "ore_perception",
    types = listOf(SkillType.FUNCTION),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, WorldRendererTrigger, ClientUseTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).also { updateOres(user) }

    override val persistTime: Int = 20 * 10

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.isUsing() && usedTime % 5 == 0) {
            updateOres(player)
        }
    }

    private fun updateOres(player: ServerPlayerEntity) {
        val world = player.world
        player.boundingBox.expand(15.0).blockPosSet
            .filter { pos -> world.isChunkLoaded(pos) }
            .filter { pos ->
                val state = world.getBlockState(pos)
                !state.isAir && (state.isTagMatches("ores") || state.isTagMatches { it.id.path.endsWith("_ores") })
            }.associateWith { pos -> getColor(world, pos) }
            .also { Channels.UPDATE_ORE_CACHE_S2C.sendToPlayer(player, UpdateOreCacheS2CPacket(it)) }
    }

    override fun renderLast(context: WorldRenderContext) {
        super.renderLast(context)
        val player = clientPlayer ?: return
        if (!player.isUsing()) return
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

    override fun onStop(player: PlayerEntity) {
        super<ClientUseTrigger>.onStop(player)
        oreCache.clear()
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

    private fun getColor(
        world: World,
        pos: BlockPos,
    ): Color {
        val state = world.getBlockState(pos)
        Colorful.getColor(world, pos, state)?.run {
            return this
        }
        return when {
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
    }

    private fun Color.lighten(distance: Double): Color = alpha(MathHelper.clamp((distance / 100).pow(2), 0.25, 1.0))

    private fun BlockState.isTagMatches(predicate: Predicate<TagKey<Block>>) = streamTags().anyMatch(predicate)

    private fun BlockState.isTagMatches(id: String) = isTagMatches { it.id.path == id }

    fun interface Colorful {

        fun getColor(world: World, pos: BlockPos, state: BlockState): Color

        companion object {

            private val colorfulBlocks = mutableMapOf<Block, Colorful>()

            fun register(block: Block, colorful: Colorful) {
                colorfulBlocks[block] = colorful
            }

            fun getColor(world: World, pos: BlockPos, state: BlockState): Color? {
                val block = state.block
                return ((block as? Colorful) ?: colorfulBlocks[block])?.getColor(world, pos, state)
            }
        }
    }

    companion object {

        private val oreCache: ConcurrentMap<BlockPos, Color> = ConcurrentHashMap()

        fun updateOreCache(blocks: Map<BlockPos, Color>) {
            oreCache.clear()
            oreCache.putAll(blocks)
        }
    }
}