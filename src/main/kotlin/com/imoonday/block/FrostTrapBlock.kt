package com.imoonday.block

import com.imoonday.block.entity.FrostTrapBlockEntity
import com.imoonday.init.ModEffects
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.SnowBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class FrostTrapBlock(settings: Settings) : SnowBlock(settings), BlockEntityProvider {

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        FrostTrapBlockEntity(pos, state)

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) = Unit

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        super.onEntityCollision(state, world, pos, entity)
        if (world.isClient) return
        if (entity !is LivingEntity) return
        if (entity is PlayerEntity && (entity.isCreative || entity.isSpectator)) return
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is FrostTrapBlockEntity && entity.uuid != blockEntity.placer) {
            entity.addStatusEffect(StatusEffectInstance(ModEffects.FREEZE, 10 * state.get(LAYERS)))
            world.breakBlock(pos, false)
        }
    }

    override fun getCollisionShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext,
    ): VoxelShape = VoxelShapes.empty()

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

    fun updatePlacer(
        world: World,
        pos: BlockPos,
        placer: LivingEntity?,
    ) {
        val entity = world.getBlockEntity(pos)
        if (entity is FrostTrapBlockEntity) {
            entity.placer = placer?.uuid
        }
    }
}