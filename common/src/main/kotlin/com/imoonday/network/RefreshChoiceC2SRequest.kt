package com.imoonday.network

import com.imoonday.util.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class RefreshChoiceC2SRequest : NetworkPacket {

    override fun encode(buf: PacketByteBuf) = Unit

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        if (!player.learnableData.isEmpty()) {
            player.refreshChoice(true)
        } else if (player.canFreshChoice()) {
            player.refreshChoice()
        }
    }
}