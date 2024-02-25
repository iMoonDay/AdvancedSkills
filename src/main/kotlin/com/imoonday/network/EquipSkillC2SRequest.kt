package com.imoonday.network

import com.imoonday.AdvancedSkills
import com.imoonday.skills.Skills
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillSlot
import com.imoonday.components.equipSkill
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class EquipSkillC2SRequest(
    val slot: SkillSlot,
    val skill: Skill,
) : FabricPacket {
    companion object {
        val id = AdvancedSkills.id("equip_skill_c2s")
        val pType = PacketType.create(id) {
            EquipSkillC2SRequest(SkillSlot.fromIndex(it.readInt()), Skills.get(it.readIdentifier()))
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                player.equipSkill(packet.slot, packet.skill)
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(slot.ordinal)
        buf.writeIdentifier(skill.id)
    }

    override fun getType(): PacketType<*> = pType
}