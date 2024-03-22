package com.imoonday.util

import net.minecraft.nbt.NbtCompound

data class SkillData(
    var cooldown: Int = 0,
    var using: Boolean = false,
    var usedTime: Int = 0,
    var usingSpeed: Int = 1,
    var data: NbtCompound = NbtCompound(),
) {

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("cooldown", cooldown)
        putBoolean("using", using)
        putInt("usedTime", usedTime)
        putInt("usingSpeed", usingSpeed)
        put("data", data)
    }

    fun copy(data: SkillData) {
        this.cooldown = data.cooldown
        this.using = data.using
        this.usedTime = data.usedTime
        this.usingSpeed = data.usingSpeed
        this.data = data.data.copy()
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
            nbt.getCompound("data")
        )
    }
}