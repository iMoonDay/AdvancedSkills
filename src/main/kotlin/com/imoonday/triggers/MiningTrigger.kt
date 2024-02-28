package com.imoonday.triggers

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface MiningTrigger {

    fun postMine(world: World, block: BlockState, pos: BlockPos, miner: PlayerEntity, item: ItemStack): Boolean
}