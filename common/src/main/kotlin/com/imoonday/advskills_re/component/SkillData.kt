package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*

data class SkillData(
    var cooldown: Int = 0,
    var using: Boolean = false,
    var usedTime: Int = 0,
    var usingSpeed: Int = 1,
    val activeData: NbtCompound = NbtCompound(),
    val persistentData: NbtCompound = NbtCompound(),
    val enhancements: MutableMap<String, EnhancementData> = mutableMapOf()
) {

    fun getOrCreateEnhancementData(id: String, level: Int? = null): EnhancementData =
        enhancements.getOrPut(id) { EnhancementData(level ?: 1) }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("cooldown", cooldown)
        putBoolean("using", using)
        putInt("usedTime", usedTime)
        putInt("usingSpeed", usingSpeed)
        put("activeData", activeData)
        put("persistentData", persistentData)
        put("enhancements", enhancements.toNbtCompound { k, v -> put(k, v.toNbt()) })
    }

    fun copyFrom(data: SkillData) {
        this.cooldown = data.cooldown
        this.using = data.using
        this.usedTime = data.usedTime
        this.usingSpeed = data.usingSpeed
        this.activeData.replaceAll(data.activeData)
        this.persistentData.replaceAll(data.persistentData)
        this.enhancements.clear()
        this.enhancements.putAll(data.enhancements)
    }

    fun tick() {
        if (cooldown > 0) {
            cooldown--
        } else if (cooldown < 0) {
            cooldown = 0
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
            nbt.getCompound("persistentData"),
            nbt.getCompound("enhancements").toStringMap { EnhancementData.fromNbt(getCompound(it)) }
        )
    }
}