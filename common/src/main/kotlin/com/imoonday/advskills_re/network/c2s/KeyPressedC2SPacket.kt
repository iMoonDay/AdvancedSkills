package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class KeyPressedC2SPacket(
    vararg val keys: Int
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(*buf.readIntArray())

    override fun encode(buf: PacketByteBuf) {
        buf.writeIntArray(keys)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
    }
}