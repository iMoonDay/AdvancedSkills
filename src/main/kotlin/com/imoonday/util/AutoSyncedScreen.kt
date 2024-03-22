package com.imoonday.util

import net.minecraft.nbt.NbtCompound

interface AutoSyncedScreen {

    fun update(data: NbtCompound = NbtCompound())
}