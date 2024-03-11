package com.imoonday.network

import com.imoonday.config.Config
import com.imoonday.util.id
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

class SyncConfigS2CPacket(
    val tag: NbtCompound,
) : FabricPacket {

    companion object {

        val id = id("sync_config_s2c")
        val pType = PacketType.create(id) {
            SyncConfigS2CPacket(it.readNbt()!!)
        }!!

        fun register() {
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                Config.instance.fromTag(packet.tag)
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeNbt(tag)
    }

    override fun getType(): PacketType<*> = pType
}