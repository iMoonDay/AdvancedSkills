package com.imoonday.advskills_re.component.choice

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.nbt.*

abstract class Choosable(val skill: Skill) {

    abstract val type: Type

    abstract fun toNbt(): NbtCompound

    abstract fun isEmpty(): Boolean

    enum class Type {
        EMPTY,
        SKILL,
        ENHANCEMENT
    }

    companion object {

        val EMPTY = object : Choosable(Skills.EMPTY) {

            override val type: Type = Type.EMPTY

            override fun toNbt(): NbtCompound = NbtCompound().also {
                it.putInt("type", type.ordinal)
            }

            override fun isEmpty(): Boolean = true
        }
    }
}