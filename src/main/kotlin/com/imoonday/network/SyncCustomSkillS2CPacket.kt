package com.imoonday.network

import com.imoonday.LOGGER
import com.imoonday.custom.CustomSkill
import com.imoonday.custom.CustomSkillHandler
import com.imoonday.util.id
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

class SyncCustomSkillS2CPacket(
    val json: String,
) : FabricPacket {

    companion object {

        val id = id("sync_custom_skill_s2c")
        val pType = PacketType.create(id) {
            SyncCustomSkillS2CPacket(it.readString())
        }!!

        fun register() {
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                try {
                    CustomSkill.fromJson(Json.parseToJsonElement(packet.json).jsonObject)?.run {
                        register()
                        CustomSkillHandler.save(this)
                    }
                } catch (e: Exception) {
                    LOGGER.warn("Failed to load json: ${packet.json}")
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeString(json)
    }

    override fun getType(): PacketType<*> = pType
}