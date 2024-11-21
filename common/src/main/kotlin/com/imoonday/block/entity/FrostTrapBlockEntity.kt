package com.imoonday.block.entity

import com.imoonday.init.*
import net.minecraft.block.*
import net.minecraft.block.entity.*
import net.minecraft.nbt.*
import net.minecraft.util.math.*
import java.util.*

class FrostTrapBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlocks.FROST_TRAP_ENTITY.get(), pos, state) {

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