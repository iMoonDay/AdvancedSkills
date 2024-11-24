package com.imoonday.network.s2c

import com.imoonday.config.*
import com.imoonday.network.*
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
        SkillConfig.instance.fromTag(tag)
    }
}