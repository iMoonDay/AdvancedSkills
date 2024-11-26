package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.client.screen.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import net.minecraft.client.sound.*
import net.minecraft.network.*

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
            if (System.currentTimeMillis() - lastPlaySoundTime > 500) {
                it.soundManager.play(PositionedSoundInstance.master(ModSounds.NOTICE.get(), 1.0f, 1.0f))
                lastPlaySoundTime = System.currentTimeMillis()
            }
        }
    }

    companion object {

        private val learningHistory = mutableListOf<Skill>()
        private var lastPlaySoundTime = 0L
    }
}