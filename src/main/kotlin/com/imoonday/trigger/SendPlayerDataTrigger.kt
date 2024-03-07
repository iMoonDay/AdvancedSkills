package com.imoonday.trigger

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

interface SendPlayerDataTrigger : SkillTrigger {

    fun write(player: ClientPlayerEntity, data: NbtCompound): NbtCompound = data

    fun apply(player: ServerPlayerEntity, data: NbtCompound) = Unit

    fun getSendTime(): SendTime = SendTime.USE

    enum class SendTime {
        USE, ALWAYS, USING, EQUIPPED
    }
}