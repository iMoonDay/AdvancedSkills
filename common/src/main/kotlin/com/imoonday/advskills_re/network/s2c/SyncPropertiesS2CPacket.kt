package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.nbt.*
import net.minecraft.network.*

class SyncPropertiesS2CPacket(
    val entityId: Int,
    val properties: NbtCompound,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readInt(), buf.readNbt()!!)

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(entityId)
        buf.writeNbt(properties)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env != EnvType.CLIENT) return
        clientPlayer?.world?.getEntityById(entityId)?.propertyComponent?.applySyncNbt(properties)
    }
}