package com.imoonday.block.entity

import com.imoonday.init.ModBlocks
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import java.util.*

class InvisibleTrapBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlocks.INVISIBLE_TRAP_ENTITY, pos, state) {

    var placer: UUID? = null
    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        if (placer != null) nbt?.putUuid("placer", placer)
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        placer = nbt?.getUuid("placer")
    }
}