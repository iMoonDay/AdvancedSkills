package com.imoonday.network

import com.imoonday.component.getSkill
import com.imoonday.trigger.SendPlayerDataTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.id
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

class UseSkillC2SRequest(
    val slot: SkillSlot,
    val keyState: KeyState,
    val data: NbtCompound,
) : FabricPacket {

    companion object {

        val id = id("use_skill_c2s")
        val pType = PacketType.create(id) {
            UseSkillC2SRequest(
                SkillSlot.fromIndex(it.readInt()),
                it.readEnumConstant(KeyState::class.java),
                it.readNbt()!!
            )
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                val slot = packet.slot
                val keyState = packet.keyState
                val data = packet.data
                if (slot.valid && !player.isSpectator) {
                    val skill = player.getSkill(slot)
                    (skill as? SendPlayerDataTrigger)
                        ?.takeIf { it.getSendTime() == SendPlayerDataTrigger.SendTime.USE }
                        ?.apply(player, data)
                    skill.tryUse(player, keyState)
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(slot.ordinal)
        buf.writeEnumConstant(keyState)
        buf.writeNbt(data)
    }

    override fun getType(): PacketType<*> = pType

    enum class KeyState {
        RELEASE,
        PRESS
    }
}