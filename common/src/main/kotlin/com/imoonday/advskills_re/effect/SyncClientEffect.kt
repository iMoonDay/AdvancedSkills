package com.imoonday.advskills_re.effect

import net.minecraft.entity.*
import net.minecraft.nbt.*

interface SyncClientEffect {

    val syncId: String

    fun writeData(entity: LivingEntity, nbt: NbtCompound = NbtCompound()): NbtCompound = nbt
}