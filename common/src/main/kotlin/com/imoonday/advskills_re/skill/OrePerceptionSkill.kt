package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import java.awt.*
import java.util.function.*

class OrePerceptionSkill : Skill(
    id = "ore_perception",
    types = listOf(SkillType.UTILITY),
    cooldown = 30,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), AutoStopTrigger, WorldRendererTrigger {

    init {
        addParameter(
            name = timeParamName,
            baseValue = 10 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).also { updateOres(user) }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.isUsing() && usedTime % 5 == 0) {
            player.server.execute {
                updateOres(player)
            }
        }
    }

    private fun updateOres(player: ServerPlayerEntity) {
        val world = player.world
        player.boundingBox.expand(15.0).blockPosSet
            .filter { pos -> world.isChunkLoaded(pos) }
            .filter { pos ->
                val state = world.getBlockState(pos)
                !state.isAir
                    && (state.isTagMatches("ores")
                    || state.isTagMatches { it.id.path.endsWith("_ores") }
                    || state.isOf(Blocks.ANCIENT_DEBRIS))
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
}