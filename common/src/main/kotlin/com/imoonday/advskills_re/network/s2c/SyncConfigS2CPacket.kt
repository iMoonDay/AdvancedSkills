package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.SyncConfigS2CPacket.ConfigType.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.nbt.*
import net.minecraft.network.*

data class SyncConfigS2CPacket(
    val tag: NbtCompound,
    val type: ConfigType,
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readNbt()!!, buf.readEnumConstant(ConfigType::class.java))

    override fun encode(buf: PacketByteBuf) {
        buf.writeNbt(tag)
        buf.writeEnumConstant(type)
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env != EnvType.CLIENT) return
        when (type) {
            GLOBAL -> GlobalConfig.get().loadFromNbt(tag)
            LOCAL -> SkillConfig.get().loadFromNbt(tag)
            BOTH -> {
                GlobalConfig.get().loadFromNbt(tag)
                SkillConfig.get().loadFromNbt(tag)
            }
        }
    }

    enum class ConfigType {
        GLOBAL, LOCAL, BOTH
    }
}