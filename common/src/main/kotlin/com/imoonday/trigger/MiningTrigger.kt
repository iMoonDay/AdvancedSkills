package com.imoonday.trigger

import net.minecraft.block.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.math.*
import net.minecraft.world.*

interface MiningTrigger : SkillTrigger {

    fun postMine(world: World, block: BlockState, pos: BlockPos, miner: PlayerEntity, item: ItemStack) = Unit
}