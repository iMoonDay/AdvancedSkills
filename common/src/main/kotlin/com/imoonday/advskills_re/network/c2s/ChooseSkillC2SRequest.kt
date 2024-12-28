package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

data class ChooseSkillC2SRequest(
    val id: Int,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readInt())

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(id)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        player.choose(id)
    }
}