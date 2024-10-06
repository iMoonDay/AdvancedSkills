package com.imoonday.network

import com.imoonday.util.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class ChooseSkillC2SRequest(
    val id: Int,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readInt())

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(id)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.player !is ServerPlayerEntity) return
        context.player.choose(id)
    }
}