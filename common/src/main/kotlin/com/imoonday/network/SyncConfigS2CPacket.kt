package com.imoonday.network

import com.imoonday.config.*
import dev.architectury.networking.*
import net.minecraft.nbt.*
import net.minecraft.network.*

class SyncConfigS2CPacket(
    val tag: NbtCompound,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readNbt()!!)

    override fun encode(buf: PacketByteBuf) {
        buf.writeNbt(tag)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        Config.instance.fromTag(tag)
    }
}