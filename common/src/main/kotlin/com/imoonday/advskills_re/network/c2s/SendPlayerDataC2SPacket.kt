package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.trigger.SendTime.*
import com.imoonday.advskills_re.util.*
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