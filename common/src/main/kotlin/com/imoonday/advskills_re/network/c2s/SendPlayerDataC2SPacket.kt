package com.imoonday.advskills_re.network.c2s

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
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
        Skills.fromId(buf.readIdentifier()),
        buf.readNbt()!!
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeIdentifier(skill.id)
        buf.writeNbt(data)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        if (!skill.invalid
            && skill is SendPlayerDataTrigger
            && player.hasLearned(skill)
            && skill.getSendTime().shouldSendOnTick(player, skill)
        ) skill.apply(player, data)
    }
}