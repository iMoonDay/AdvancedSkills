package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*
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