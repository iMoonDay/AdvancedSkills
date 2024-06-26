package com.imoonday.util

import net.minecraft.nbt.NbtCompound

data class SkillLevelData(
    var experience: Int = 0,
    var level: Int = 0,
    var cycle: Int = 0,
) {

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("experience", experience)
        putInt("level", level)
        putInt("cycle", cycle)
    }

    fun copyFrom(data: SkillLevelData) {
        this.experience = data.experience
        this.level = data.level
        this.cycle = data.cycle
    }

    companion object {

        fun fromNbt(nbt: NbtCompound): SkillLevelData = SkillLevelData(
            nbt.getInt("experience"),
            nbt.getInt("level"),
            nbt.getInt("cycle")
        )
    }
}
