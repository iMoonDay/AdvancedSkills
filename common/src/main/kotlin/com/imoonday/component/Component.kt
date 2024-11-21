package com.imoonday.component

import net.minecraft.nbt.*

interface Component {

    fun readFromNbt(tag: NbtCompound)

    fun writeToNbt(tag: NbtCompound)

    fun tick() = Unit

    fun serverTick() = tick()

    fun clientTick() = tick()

    fun toNbt(): NbtCompound =
        NbtCompound().apply { writeToNbt(this) }

    fun applySyncNbt(tag: NbtCompound) = readFromNbt(tag)

    fun sync()
}
