package com.imoonday.network

import com.imoonday.trigger.*
import com.imoonday.util.*
import dev.architectury.networking.NetworkManager.*
import net.minecraft.nbt.*
import net.minecraft.network.*
import net.minecraft.server.network.*

class UseSkillC2SRequest(
    val slot: Int,
    val keyState: KeyState,
    val data: NbtCompound,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        buf.readInt(),
        buf.readEnumConstant(KeyState::class.java),
        buf.readNbt()!!
    )

    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(slot)
        buf.writeEnumConstant(keyState)
        buf.writeNbt(data)
    }

    override fun apply(context: PacketContext) {
        val player = context.player as? ServerPlayerEntity ?: return
        if (SkillSlot.isValidIndex(player, slot) && !player.isSpectator) {
            val skill = player.getSkill(slot)
            (skill as? SendPlayerDataTrigger)
                ?.takeIf { it.getSendTime() == SendTime.USE }
                ?.apply(player, data)
            skill.tryUse(player, keyState)
        }
    }

    enum class KeyState {
        RELEASE,
        PRESS
    }
}