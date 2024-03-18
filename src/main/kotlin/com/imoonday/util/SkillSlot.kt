package com.imoonday.util

enum class SkillSlot {
    INVALID,
    SLOT_1,
    SLOT_2,
    SLOT_3,
    SLOT_4;

    val valid: Boolean
        get() = this != INVALID

    companion object {

        /**
         * @param index 1~4
         */
        fun fromIndex(index: Int): SkillSlot = if (index in 0..<entries.size) entries[index] else INVALID
    }
}