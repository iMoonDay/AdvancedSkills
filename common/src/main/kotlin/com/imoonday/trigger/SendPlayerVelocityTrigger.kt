package com.imoonday.trigger

import com.imoonday.util.*
import net.minecraft.client.network.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

interface SendPlayerVelocityTrigger : SendPlayerDataTrigger {

    override fun write(player: ClientPlayerEntity, data: NbtCompound): NbtCompound =
        NbtUtils.writeVec3dToTag(player.velocity, data)

    override fun apply(player: ServerPlayerEntity, data: NbtCompound) {
        NbtUtils.readVec3d(data)?.let { player.velocity = it }
    }
}