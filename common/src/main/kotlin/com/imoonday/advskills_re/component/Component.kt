package com.imoonday.advskills_re.component

import net.minecraft.entity.*
import net.minecraft.nbt.*

interface Component<T : Entity> {

    val entity: T
    var synced: Boolean

    fun readFromNbt(tag: NbtCompound)

    fun writeToNbt(tag: NbtCompound)

    fun tick() = Unit

    fun serverTick() = tick()

    fun clientTick() {
        if (!synced && entity.age % 20 == 0) {
            requestSync()
        }
        tick()
    }

    fun toNbt(): NbtCompound =
        NbtCompound().apply { writeToNbt(this) }

    fun applySyncNbt(tag: NbtCompound) {
        readFromNbt(tag)
        synced = true
    }

    fun sync()

    fun requestSync()
}
