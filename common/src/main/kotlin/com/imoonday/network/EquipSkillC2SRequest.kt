package com.imoonday.network

import com.imoonday.skill.*
import com.imoonday.util.*
import dev.architectury.networking.NetworkManager.*
import net.minecraft.network.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class EquipSkillC2SRequest(
    val slot: Int,
    val skill: Skill,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        buf.readInt(),
        Skill.fromId(buf.readIdentifier())
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(slot)
        buf.writeIdentifier(skill.id)
    }

    override fun apply(context: PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        if (player.equip(skill, slot) && !skill.invalid) {
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC)
        }
    }
}