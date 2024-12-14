package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class RefreshChoiceC2SRequest(val type: Type) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readEnumConstant(Type::class.java))

    override fun encode(buf: PacketByteBuf) {
        buf.writeEnumConstant(type)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        if (!player.canFreshChoice(type)) return

        player.refreshSkillChoice(type)
    }

    enum class Type {
        SKILL, ENHANCEMENT
    }
}