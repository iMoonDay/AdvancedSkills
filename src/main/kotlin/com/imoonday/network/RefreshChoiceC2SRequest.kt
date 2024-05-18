package com.imoonday.network

import com.imoonday.util.canFreshChoice
import com.imoonday.util.id
import com.imoonday.util.learnableData
import com.imoonday.util.refreshChoice
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class RefreshChoiceC2SRequest : FabricPacket {

    companion object {

        val id = id("refresh_choice_c2s")
        val pType = PacketType.create(id) {
            RefreshChoiceC2SRequest()
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { _, player, _ ->
                if (!player.learnableData.isEmpty()) {
                    player.refreshChoice(true)
                } else if (player.canFreshChoice()) {
                    player.refreshChoice()
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) = Unit

    override fun getType(): PacketType<*> = pType
}