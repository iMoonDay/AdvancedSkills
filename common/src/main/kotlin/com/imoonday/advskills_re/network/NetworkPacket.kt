package com.imoonday.advskills_re.network

import dev.architectury.networking.NetworkManager.*
import net.minecraft.network.*

interface NetworkPacket {

    fun encode(buf: PacketByteBuf)

    fun apply(context: PacketContext)
}