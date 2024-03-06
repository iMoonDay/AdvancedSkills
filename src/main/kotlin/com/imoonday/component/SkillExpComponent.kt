package com.imoonday.component

import com.imoonday.init.ModComponents
import com.imoonday.util.updateScreen
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

interface LongComponent : Component {
    var value: Long
}

class SkillExpComponent(private val provider: PlayerEntity) : LongComponent, AutoSyncedComponent {
    override var value: Long = 0
        set(value) {
            field = value
            ModComponents.EXP.sync(provider)
        }

    override fun readFromNbt(tag: NbtCompound) {
        value = tag.getLong("value")
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.putLong("value", value)
    }

    override fun writeSyncPacket(buf: PacketByteBuf, recipient: ServerPlayerEntity) {
        buf.writeVarLong(value)
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        value = buf.readVarLong()
        provider.updateScreen()
    }
}

var PlayerEntity.skillExp: Long
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

val levelExpCache = IntArray(101) { -1 }

fun getNextLevelExp(level: Int): Int {
    if (levelExpCache[level] != -1) {
        return levelExpCache[level]
    }

    val exp = when {
        level >= 30 -> 112 + (level - 30) * 9
        level >= 15 -> 37 + (level - 15) * 5
        else -> 7 + level * 2
    }

    levelExpCache[level] = exp
    return exp
}

val PlayerEntity.skillLevel: Int
    get() {
        var level = 0
        var reset = 0
        var needed = getNextLevelExp(level)

        var experience = skillExp
        while (experience >= needed) {
            experience -= needed
            if (++level > 100) {
                if (++reset > 99) {
                    level = -1
                    break
                }
                level = reset
            }
            needed = getNextLevelExp(level)
        }

        return level + 100 * reset
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