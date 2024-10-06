package com.imoonday.network

import com.imoonday.skill.*
import com.imoonday.trigger.*
import com.imoonday.trigger.SendTime.*
import com.imoonday.util.*
import dev.architectury.networking.*
import net.minecraft.nbt.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class SendPlayerDataC2SPacket(
    val skill: Skill,
    val data: NbtCompound,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        Skill.fromId(buf.readIdentifier()),
        buf.readNbt()!!
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
        buf.writeNbt(data)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
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