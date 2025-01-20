package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import java.awt.*
import java.util.function.*

class OrePerceptionSkill : Skill(
    Settings(
        id = "ore_perception",
        types = listOf(SkillType.UTILITY),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
), AutoStopTrigger, WorldRendererTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_ORE_BLOCKS, emptyList<String>())
            .addParameter(PARAM_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL)
            .addParameter(
                name = PARAM_DETECT_RANGE,
                baseValue = DEFAULT_DETECT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DETECT_DURATION,
                baseValue = DEFAULT_DETECT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        updateOres(user)
    }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_DETECT_DURATION, player, DEFAULT_DETECT_DURATION, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        val interval = getIntParam(PARAM_UPDATE_INTERVAL, player, DEFAULT_UPDATE_INTERVAL, 1)
        if (player.isUsing() && usedTime % interval == 0) {
            player.server.execute {
                updateOres(player)
            }
        }
    }

    private fun updateOres(player: ServerPlayerEntity) {
        val world = player.world
        val range = getDoubleParam(PARAM_DETECT_RANGE, player, DEFAULT_DETECT_RANGE)
        val oreBlocks = getListParam(PARAM_ORE_BLOCKS).getStringList()
        player.boundingBox.expand(range).blockPosSet
            .filter { pos -> world.isChunkLoaded(pos) }
            .filter { pos ->
                val state = world.getBlockState(pos)
                !state.isAir
                    && (state.isTagMatches("ores")
                    || state.isTagMatches { it.id.path.endsWith("_ores") }
                    || state.isOf(Blocks.ANCIENT_DEBRIS)
                    || oreBlocks.contains(Registries.BLOCK.getId(state.block).toString()))
            }.associateWith { pos -> getColor(world, pos) }
            .also { Channels.UPDATE_ORE_CACHE_S2C.sendToPlayer(player, UpdateOreCacheS2CPacket(it)) }
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
            state.isOf(Blocks.ANCIENT_DEBRIS) -> Color.MAGENTA
            else -> Color.WHITE
        }
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

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

        // Default Values
        private const val DEFAULT_UPDATE_INTERVAL = 5
        private const val DEFAULT_DETECT_RANGE = 15.0
        private const val DEFAULT_DETECT_DURATION = 10 * 20

        // Parameter Names
        private const val PARAM_UPDATE_INTERVAL = "update_interval"  // 更新间隔
        private const val PARAM_DETECT_RANGE = "detect_range"  // 探测范围
        private const val PARAM_DETECT_DURATION = "detect_duration"  // 探测持续时间
        private const val PARAM_ORE_BLOCKS = "ore_blocks"  // 矿石方块列表

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应探测范围
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}