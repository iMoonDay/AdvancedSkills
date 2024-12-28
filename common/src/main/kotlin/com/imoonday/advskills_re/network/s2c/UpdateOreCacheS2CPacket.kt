package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.client.render.skill.special.*
import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.network.*
import net.minecraft.util.math.*
import java.awt.*

data class UpdateOreCacheS2CPacket(
    private val colorMap: Map<BlockPos, Color>
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(
        buf.readMap(PacketByteBuf::readBlockPos) { buf1 -> Color(buf1.readInt()) }
    )

    override fun encode(buf: PacketByteBuf) =
        buf.writeMap(colorMap, PacketByteBuf::writeBlockPos) { buf1, color -> buf1.writeInt(color.rgb) }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env != EnvType.CLIENT) return
        OrePerceptionSkillRenderer.updateOreCache(colorMap)
    }
}