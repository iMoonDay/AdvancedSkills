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

    fun getSendTime(): SendTime = SendTime.ON_USE

    fun shouldSendData(player: PlayerEntity): Boolean = false
}

enum class SendTime(val isOnUse: Boolean) {
    /**
     * Send data when requesting using the skill
     */
    ON_USE(true) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean = false
    },

    /**
     * Send data when player has equipped the skill
     */
    ALWAYS(false) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean = true
    },

    /**
     * Send data when player is using the skill
     */
    USING(false) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean = player.isUsing(skill)
    },

    /**
     * Send data while custom predicate is true
     */
    PREDICATE(false) {

        override fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean =
            skill is SendPlayerDataTrigger && skill.shouldSendData(player)
    };

    abstract fun shouldSendOnTick(player: PlayerEntity, skill: Skill): Boolean
}