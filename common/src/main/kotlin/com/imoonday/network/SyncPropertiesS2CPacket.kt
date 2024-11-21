package com.imoonday.network

import com.imoonday.component.*
import com.imoonday.util.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.nbt.*
import net.minecraft.network.*

class SyncPropertiesS2CPacket(
    val entityId: Int,
    val properties: NbtCompound
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readInt(), buf.readNbt()!!)

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(entityId)
        buf.writeNbt(properties)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env == EnvType.CLIENT) {
            clientPlayer?.world?.getEntityById(entityId)?.propertyComponent?.applySyncNbt(properties)
        }
    }
}