package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.network.*
import dev.architectury.networking.*
import net.fabricmc.api.*
import net.minecraft.nbt.*
import net.minecraft.network.*

data class SyncRaritiesS2CPacket(
    val rarities: List<SkillRarity>
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readList { SkillRarity.fromNbt(it.readNbt() ?: NbtCompound()) })

    override fun encode(buf: PacketByteBuf) {
        buf.writeCollection(rarities) { buf1, rarity -> buf1.writeNbt(rarity.toNbt()) }
    }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.env != EnvType.CLIENT) return
        SkillRarity.updateRarities(rarities)
    }
}