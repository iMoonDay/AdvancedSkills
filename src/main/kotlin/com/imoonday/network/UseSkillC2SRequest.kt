package com.imoonday.network

import com.imoonday.components.getSkill
import com.imoonday.utils.id
import com.imoonday.utils.SkillSlot
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class UseSkillC2SRequest(
    val slot: SkillSlot,
    val keyState: KeyState,
) : FabricPacket {
    companion object {
        val id = id("use_skill_c2s")
        val pType = PacketType.create(id) {
            UseSkillC2SRequest(SkillSlot.fromIndex(it.readInt()), it.readEnumConstant(KeyState::class.java))
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                val slot = packet.slot
                val keyState = packet.keyState
                if (slot.valid && !player.isSpectator) {
                    player.getSkill(slot).tryUse(player, keyState)
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(slot.ordinal)
        buf.writeEnumConstant(keyState)
    }

    override fun getType(): PacketType<*> = pType

    enum class KeyState {
        RELEASE,
        PRESS
    }
}