package com.imoonday.advskills_re.trigger

import net.minecraft.nbt.*
import net.minecraft.server.network.*

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