package com.imoonday.trigger

import fi.dy.masa.malilib.util.NBTUtils
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

interface SendPlayerVelocityTrigger : SendPlayerDataTrigger {

    override fun write(player: ClientPlayerEntity, data: NbtCompound): NbtCompound =
        NBTUtils.writeVec3dToTag(player.velocity, data)

    override fun apply(player: ServerPlayerEntity, data: NbtCompound) {
        NBTUtils.readVec3d(data)?.let { player.velocity = it }
    }
}