package com.imoonday.trigger

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

interface AutoTrigger : SkillTrigger {

    fun shouldStart(player: ServerPlayerEntity): Boolean

    fun shouldStop(player: ServerPlayerEntity): Boolean = false

    fun writeData(player: ServerPlayerEntity): (NbtCompound) -> Unit = {}

    fun tick(player: ServerPlayerEntity) {
        if (shouldStart(player) && !player.isUsing() && !shouldStop(player)) {
            player.startUsing(writeData(player))
        }
        if (player.isUsing() && shouldStop(player)) {
            player.stopUsing()
        }
    }
}