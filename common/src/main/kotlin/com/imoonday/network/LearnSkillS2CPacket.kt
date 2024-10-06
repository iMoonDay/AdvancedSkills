package com.imoonday.network

import com.imoonday.init.*
import com.imoonday.screen.component.*
import com.imoonday.skill.*
import com.imoonday.util.*
import dev.architectury.networking.*
import net.minecraft.client.sound.*
import net.minecraft.network.*
import net.minecraft.util.*

class LearnSkillS2CPacket(
    val skill: Skill,
    val toast: Boolean,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        Skill.fromId(buf.readIdentifier()),
        buf.readBoolean()
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
        buf.writeBoolean(toast)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        client?.let {
            if (skill !in learningHistory && toast) {
                learningHistory.add(skill)
                it.toastManager.add(SkillToast(skill))
            }
            if (Util.getMeasuringTimeMs() - lastPlaySoundTime > 500) {
                it.soundManager.play(PositionedSoundInstance.master(ModSounds.NOTICE, 1.0f, 1.0f))
                lastPlaySoundTime = Util.getMeasuringTimeMs()
            }
        }
    }

    companion object {

        private val learningHistory = mutableListOf<Skill>()
        private var lastPlaySoundTime = 0L
    }
}