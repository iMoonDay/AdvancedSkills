package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import dev.architectury.utils.*
import net.minecraft.client.sound.*
import net.minecraft.network.*
import net.minecraft.text.*

data class EnhanceSkillS2CPacket(
    val message: Text
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readText())

    override fun encode(buf: PacketByteBuf) {
        buf.writeText(message)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.environment != Env.CLIENT) return
        client?.let {
            if (System.currentTimeMillis() - lastPlaySoundTime > 500) {
                it.soundManager.play(PositionedSoundInstance.master(ModSounds.ENHANCE.get(), 1.0f, 1.0f))
                lastPlaySoundTime = System.currentTimeMillis()
            }

            if (message.content != TextContent.EMPTY) {
                it.inGameHud.setOverlayMessage(message, false)
            }
        }
    }

    companion object {

        private var lastPlaySoundTime = 0L
    }
}