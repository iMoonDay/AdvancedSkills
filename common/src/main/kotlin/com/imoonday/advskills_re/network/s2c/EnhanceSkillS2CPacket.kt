package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import dev.architectury.utils.*
import net.minecraft.client.sound.*
import net.minecraft.network.*

object EnhanceSkillS2CPacket : NetworkPacket {

    private var lastPlaySoundTime = 0L

    override fun encode(buf: PacketByteBuf) = Unit

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.environment != Env.CLIENT) return
        client?.let {
            if (System.currentTimeMillis() - lastPlaySoundTime > 500) {
                it.soundManager.play(PositionedSoundInstance.master(ModSounds.ENHANCE.get(), 1.0f, 1.0f))
                lastPlaySoundTime = System.currentTimeMillis()
            }
        }
    }
}