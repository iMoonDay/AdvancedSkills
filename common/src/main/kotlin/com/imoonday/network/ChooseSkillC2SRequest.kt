package com.imoonday.network

import com.imoonday.util.choose
import com.imoonday.util.id
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class ChooseSkillC2SRequest(
    val id: Int,
) : FabricPacket {

    companion object {

        val id = id("choose_skill_c2s")
        val pType = PacketType.create(id) {
            ChooseSkillC2SRequest(it.readInt())
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                player.choose(packet.id)
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(id)
    }

    override fun getType(): PacketType<*> = pType
}