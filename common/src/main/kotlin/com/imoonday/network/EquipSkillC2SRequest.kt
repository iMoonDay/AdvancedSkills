package com.imoonday.network

import com.imoonday.skill.Skill
import com.imoonday.util.equip
import com.imoonday.util.id
import com.imoonday.util.playSound
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.sound.SoundEvents

class EquipSkillC2SRequest(
    val slot: Int,
    val skill: Skill,
) : FabricPacket {

    companion object {

        val id = id("equip_skill_c2s")
        val pType = PacketType.create(id) {
            EquipSkillC2SRequest(it.readInt(), Skill.fromId(it.readIdentifier()))
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                val skill = packet.skill
                if (player.equip(skill, packet.slot) && !skill.invalid) {
                    player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC)
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(slot)
        buf.writeIdentifier(skill.id)
    }

    override fun getType(): PacketType<*> = pType
}