package com.imoonday.components

import com.imoonday.init.ModComponents
import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Util

interface LongComponent : Component {
    var first: Long
    var second: Long
}

class DamagedTimeComponent(private val provider: PlayerEntity) : LongComponent {
    override var first: Long = 0
    override var second: Long = 0

    override fun readFromNbt(tag: NbtCompound) {
        first = tag.getLong("damagedTime")
        second = tag.getLong("reflectedTime")
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.putLong("damagedTime", first)
        tag.putLong("reflectedTime", second)
    }
}

var ServerPlayerEntity.lastDamagedTime: Long
    get() = getComponent(ModComponents.DAMAGED_TIME).first
    set(value) {
        getComponent(ModComponents.DAMAGED_TIME).first = value
    }

fun ServerPlayerEntity.onDamage() {
    lastDamagedTime = Util.getMeasuringTimeMs()
    val l = lastDamagedTime - lastReflectedTime
    if (l < 1000) {
        sendMessage(Text.translatable("advancedSkills.skill.extreme_reflection.early", (l / 1000.0).toString()), true)
        lastReflectedTime = 0
        lastDamagedTime = 0
    }
}

var ServerPlayerEntity.lastReflectedTime: Long
    get() = getComponent(ModComponents.DAMAGED_TIME).second
    set(value) {
        getComponent(ModComponents.DAMAGED_TIME).second = value
    }