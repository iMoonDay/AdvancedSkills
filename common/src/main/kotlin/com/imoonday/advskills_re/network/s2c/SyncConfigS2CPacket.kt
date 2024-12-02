package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.nbt.*
import net.minecraft.network.*

class SyncConfigS2CPacket(
    val tag: NbtCompound,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readNbt()!!)

    override fun encode(buf: PacketByteBuf) {
        buf.writeNbt(tag)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env != EnvType.CLIENT) return
        SkillConfig.get().load(tag)
    }
}