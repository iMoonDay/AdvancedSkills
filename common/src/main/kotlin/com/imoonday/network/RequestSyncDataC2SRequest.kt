package com.imoonday.network

import com.imoonday.util.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class RequestSyncDataC2SRequest : NetworkPacket {

    override fun encode(buf: PacketByteBuf) = Unit

    override fun apply(context: NetworkManager.PacketContext) {
        (context.player as? ServerPlayerEntity)?.syncData()
    }
}