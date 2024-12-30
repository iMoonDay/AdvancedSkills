package com.imoonday.advskills_re.component

import net.minecraft.nbt.*

class EnhancementData(maxLevel: Int = 1, var activated: Boolean = true) {

    var maxLevel: Int = maxLevel
        set(value) {
            val oldValue = field
            val newValue = value.coerceAtLeast(1)
            field = newValue
            if (currentLevel == oldValue) {
                currentLevel = field
            }
        }
    var currentLevel: Int = maxLevel
        get() {
            if (field > maxLevel) {
                field = maxLevel
            } else if (field < 0) {
                field = 0
            }
            return field
        }
        set(value) {
            field = value.coerceIn(0, maxLevel)
        }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("maxLevel", maxLevel)
        putInt("currentLevel", currentLevel)
        putBoolean("activated", activated)
    }

    companion object {

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): EnhancementData = EnhancementData(
            nbt.getInt("maxLevel"),
            nbt.getBoolean("activated")
        )
    }
}
