package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

interface SendPlayerDataTrigger : SkillTrigger {

    fun write(player: PlayerEntity, data: NbtCompound): NbtCompound = data

    fun apply(player: ServerPlayerEntity, data: NbtCompound) = Unit

    fun getSendTime(): SendTime = SendTime.USE
}

enum class SendTime {
    USE, ALWAYS, USING, EQUIPPED
}