package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.skill.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.network.*

data class SyncSettingsS2CPacket(
    val settings: List<Skill.Settings>
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readList { Skill.Settings.fromNbt(it.readNbt()!!)!! })

    override fun encode(buf: PacketByteBuf) {
        buf.writeCollection(settings) { buf1, settings1 -> buf1.writeNbt(settings1.toNbt()) }
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env != EnvType.CLIENT) return
        Skills.updateSettings(settings)
    }
}