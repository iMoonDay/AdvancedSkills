package com.imoonday.block

import com.imoonday.block.entity.InvisibleTrapBlockEntity
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

class InvisibleTrapBlock(settings: Settings) : BlockWithEntity(settings), Waterloggable {

    init {
        defaultState = defaultState.with(WATERLOGGED, false)
    }

    override fun isTransparent(state: BlockState, world: BlockView, pos: BlockPos): Boolean = true

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? =
        defaultState.with(WATERLOGGED, ctx.world.getFluidState(ctx.blockPos).fluid == Fluids.WATER)

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext,
    ): VoxelShape = OUTLINE_SHAPE

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        super.onEntityCollision(state, world, pos, entity)
        if (world.isClient) return
        if (entity !is LivingEntity) return
        if (entity is PlayerEntity && (entity.isCreative || entity.isSpectator)) return
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is InvisibleTrapBlockEntity && entity.uuid != blockEntity.placer) {
            entity.slowMovement(state, Vec3d(0.25, 1.0, 0.25))
            entity.damage(world.damageSources.magic(), 2.0f)
            world.breakBlock(pos, false)
        }
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack,
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        updatePlacer(world, pos, placer)
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val blockPos = pos.down()
        return hasTopRim(world, blockPos) || sideCoversSmallSquare(world, blockPos, Direction.UP)
    }

    fun updatePlacer(
        world: World,
        pos: BlockPos,
        placer: LivingEntity?,
    ) {
        val entity = world.getBlockEntity(pos)
        if (entity is InvisibleTrapBlockEntity) {
            entity.placer = placer?.uuid
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        InvisibleTrapBlockEntity(pos, state)

    override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.INVISIBLE

    override fun spawnBreakParticles(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState?) {
        super.spawnBreakParticles(world, player, pos, state)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos?,
    ): BlockState {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.defaultState
        }
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun getFluidState(state: BlockState): FluidState =
        if (state.get(WATERLOGGED)) {
            Fluids.WATER.getStill(false)
        } else
            super.getFluidState(state)

    companion object {

        val OUTLINE_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0)
        val WATERLOGGED: BooleanProperty = Properties.WATERLOGGED
    }
}