package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.network.*

class SyncPlayerDataS2CPacket(
    val playerId: Int,
    val playerData: NbtCompound,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readInt(), buf.readNbt()!!)

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(playerId)
        buf.writeNbt(playerData)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env != EnvType.CLIENT) return
        (clientPlayer?.world?.getEntityById(playerId) as? PlayerEntity)?.data?.applySyncNbt(playerData)
    }
}