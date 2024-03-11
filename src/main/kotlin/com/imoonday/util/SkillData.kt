package com.imoonday.util

import net.minecraft.nbt.NbtCompound

data class SkillData(
    var cooldown: Int,
    var using: Boolean,
    var usedTime: Int,
    var data: NbtCompound,
) {

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("cooldown", cooldown)
        putBoolean("using", using)
        putInt("usedTime", usedTime)
        put("data", data)
    }

    fun copy(data: SkillData) {
        this.cooldown = data.cooldown
        this.using = data.using
        this.usedTime = data.usedTime
        this.data = data.data.copy()
    }

    fun tick() {
        if (cooldown > 0) {
            cooldown--
        }
        if (using) {
            usedTime++
        } else if (usedTime != 0) {
            usedTime = 0
        }
    }

    companion object {

        fun fromNbt(nbt: NbtCompound): SkillData = SkillData(
            nbt.getInt("cooldown"),
            nbt.getBoolean("using"),
            nbt.getInt("usedTime"),
            nbt.getCompound("data")
        )
    }
}