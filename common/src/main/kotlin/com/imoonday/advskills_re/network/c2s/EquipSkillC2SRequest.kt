package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.NetworkManager.*
import net.minecraft.network.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

data class EquipSkillC2SRequest(
    val slot: Int,
    val skill: Skill,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        buf.readInt(),
        Skills.fromId(buf.readIdentifier())
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