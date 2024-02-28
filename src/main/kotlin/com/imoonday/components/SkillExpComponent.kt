package com.imoonday.components

import com.imoonday.init.ModComponents
import com.imoonday.utils.updateScreen
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

interface IntComponent : Component {
    var value: Int
}

class SkillExpComponent(private val provider: PlayerEntity) : IntComponent, AutoSyncedComponent {
    override var value: Int = 0
        set(value) {
            field = value
            ModComponents.EXP.sync(provider)
        }

    override fun readFromNbt(tag: NbtCompound) {
        value = tag.getInt("value")
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.putInt("value", value)
    }

    override fun writeSyncPacket(buf: PacketByteBuf, recipient: ServerPlayerEntity) {
        buf.writeVarInt(value)
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        value = buf.readVarInt()
        provider.updateScreen()
    }
}

var PlayerEntity.skillExp: Int
    get() = getComponent(ModComponents.EXP).value
    set(value) {
        val i = skillLevel
        getComponent(ModComponents.EXP).value = value
        val level = skillLevel
        if (level > i) {
            (i + 1..level).filter { shouldLearnSkill(level) }
                .forEach { _ ->
                    learnRandomSkill()
                }
        }
    }

fun getNextLevelExp(level: Int): Int {
    if (level >= 30) {
        return 112 + (level - 30) * 9
    }
    if (level >= 15) {
        return 37 + (level - 15) * 5
    }
    return 7 + level * 2
}

val PlayerEntity.skillLevel: Int
    get() {
        var level = 0
        var needed = getNextLevelExp(level)

        var experience = skillExp
        while (experience >= needed) {
            experience -= needed
            level++
            needed = getNextLevelExp(level)
        }

        return level
    }

fun shouldLearnSkill(level: Int): Boolean {
    return when {
        level <= 0 -> false
        level in 1..14 -> level % 5 == 0
        level in 15..29 -> level % 3 == 0
        level in 30..80 -> level % 2 == 0
        else -> true
    }
}