package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.screen.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import dev.architectury.networking.*
import dev.architectury.utils.*
import net.minecraft.client.sound.*
import net.minecraft.network.*

data class LearnSkillS2CPacket(
    val skill: Skill,
    val toast: Boolean,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        Skills.fromId(buf.readIdentifier()),
        buf.readBoolean()
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
        buf.writeBoolean(toast)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.environment != Env.CLIENT) return
        client?.let {
            val skills = ClientConfig.get().displayedSkills
            if (skill.id.toString() !in skills && toast) {
                skills.add(skill.id.toString())
                it.toastManager.add(SkillToast(skill))
                ClientConfig.get().save()
            }
            if (System.currentTimeMillis() - lastPlaySoundTime > 500) {
                it.soundManager.play(PositionedSoundInstance.master(ModSounds.NOTICE.get(), 1.0f, 1.0f))
                lastPlaySoundTime = System.currentTimeMillis()
            }
        }
    }

    companion object {

        private var lastPlaySoundTime = 0L
    }
}