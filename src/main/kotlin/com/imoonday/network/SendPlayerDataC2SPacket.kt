package com.imoonday.network

import com.imoonday.skill.Skill
import com.imoonday.trigger.SendPlayerDataTrigger
import com.imoonday.trigger.SendTime.*
import com.imoonday.util.hasEquipped
import com.imoonday.util.hasLearned
import com.imoonday.util.id
import com.imoonday.util.isUsing
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

class SendPlayerDataC2SPacket(
    val skill: Skill,
    val data: NbtCompound,
) : FabricPacket {

    companion object {

        val id = id("send_player_data_c2s")
        val pType = PacketType.create(id) {
            SendPlayerDataC2SPacket(Skill.fromId(it.readIdentifier()), it.readNbt()!!)
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                val skill = packet.skill
                val data = packet.data
                if (!skill.invalid && skill is SendPlayerDataTrigger && player.hasLearned(skill)) {
                    when (skill.getSendTime()) {
                        ALWAYS -> skill.apply(player, data)
                        USING -> if (player.isUsing(skill)) skill.apply(player, data)
                        EQUIPPED -> if (player.hasEquipped(skill)) skill.apply(player, data)
                        else -> {}
                    }
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
        buf.writeNbt(data)
    }

    override fun getType(): PacketType<*> = pType
}