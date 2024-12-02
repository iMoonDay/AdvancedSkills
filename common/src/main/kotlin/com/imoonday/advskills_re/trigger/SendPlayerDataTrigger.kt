package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

interface SendPlayerDataTrigger : SkillTrigger {

    fun write(player: PlayerEntity, data: NbtCompound): NbtCompound

    fun apply(player: ServerPlayerEntity, data: NbtCompound) {
        player.getPersistentData().copyFrom(data)
    }

    fun getSendTime(): SendTime = SendTime.USE
}

enum class SendTime(val isOnUse: Boolean) {
    USE(true) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean = false
    },
    ALWAYS(false) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean = true
    },
    USING(false) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean = player.isUsing(skill)
    },
    EQUIPPED(false) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean = player.hasEquipped(skill)
    };

    abstract fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean
}