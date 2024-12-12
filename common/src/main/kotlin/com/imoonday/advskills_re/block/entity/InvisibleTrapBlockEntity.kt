package com.imoonday.advskills_re.block.entity

import com.imoonday.advskills_re.init.*
import net.minecraft.block.*
import net.minecraft.block.entity.*
import net.minecraft.nbt.*
import net.minecraft.util.math.*
import java.util.*

class InvisibleTrapBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlocks.INVISIBLE_TRAP_ENTITY.get(), pos, state) {

    var placer: UUID? = null
    var damage: Float = 2.0f

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        if (placer != null) nbt.putUuid("placer", placer)
        nbt.putFloat("damage", damage)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        if (nbt.containsUuid("placer")) {
            placer = nbt.getUuid("placer")
        }
        if (nbt.contains("damage")) {
            damage = nbt.getFloat("damage")
        }
    }
}