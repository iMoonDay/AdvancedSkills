package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

interface SendPlayerVelocityTrigger : SendPlayerDataTrigger {

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound =
        NbtUtils.writeVec3dToTag(player.velocity, data)

    override fun apply(player: ServerPlayerEntity, data: NbtCompound) {
        NbtUtils.readVec3d(data)?.let { player.velocity = it }
    }
}