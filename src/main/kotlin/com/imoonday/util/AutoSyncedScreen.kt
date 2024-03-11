package com.imoonday.util

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

interface AutoSyncedScreen {

    fun update(data: NbtCompound)
}

fun PlayerEntity.updateScreen(data: NbtCompound = NbtCompound()) {
    if (world.isClient) {
        (MinecraftClient.getInstance().currentScreen as? AutoSyncedScreen)?.update(data)
    }
}