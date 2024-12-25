package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class UpdateJumpingC2SPacket(
    val jumping: Boolean
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readBoolean())

    override fun encode(buf: PacketByteBuf) {
        buf.writeBoolean(jumping)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        (player as LivingEntityAccessor).isJumping = jumping
    }
}