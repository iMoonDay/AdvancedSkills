package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*

data class SkillData(
    var cooldown: Int = 0,
    var using: Boolean = false,
    var usedTime: Int = 0,
    var usingSpeed: Int = 1,
    val activeData: NbtCompound = NbtCompound(),
    val persistentData: NbtCompound = NbtCompound()
) {

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("cooldown", cooldown)
        putBoolean("using", using)
        putInt("usedTime", usedTime)
        putInt("usingSpeed", usingSpeed)
        put("activeData", activeData)
        put("persistentData", persistentData)
    }

    fun copy(data: SkillData) {
        this.cooldown = data.cooldown
        this.using = data.using
        this.usedTime = data.usedTime
        this.usingSpeed = data.usingSpeed
        this.activeData.replaceAll(data.activeData)
        this.persistentData.replaceAll(data.persistentData)
    }

    fun tick() {
        if (cooldown > 0) {
            cooldown--
        }
        if (using) {
            usedTime += usingSpeed
        } else if (usedTime != 0) {
            usedTime = 0
        }
    }

    companion object {

        fun fromNbt(nbt: NbtCompound): SkillData = SkillData(
            nbt.getInt("cooldown"),
            nbt.getBoolean("using"),
            nbt.getInt("usedTime"),
            if (nbt.contains("usingSpeed")) nbt.getInt("usingSpeed") else 1,
            nbt.getCompound("activeData"),
            nbt.getCompound("persistentData")
        )
    }
}