package com.imoonday.trigger

import net.minecraft.client.network.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

interface SendPlayerDataTrigger : SkillTrigger {

    fun write(player: ClientPlayerEntity, data: NbtCompound): NbtCompound = data

    fun apply(player: ServerPlayerEntity, data: NbtCompound) = Unit

    fun getSendTime(): SendTime = SendTime.USE
}

enum class SendTime {
    USE, ALWAYS, USING, EQUIPPED
}