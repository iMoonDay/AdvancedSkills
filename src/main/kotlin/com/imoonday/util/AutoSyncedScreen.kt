package com.imoonday.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

interface AutoSyncedScreen {

    fun update(data: NbtCompound = NbtCompound())
}

fun PlayerEntity.updateScreen(data: NbtCompound = NbtCompound()) {
    if (world.isClient) {
        (client!!.currentScreen as? AutoSyncedScreen)?.update(data)
    }
}