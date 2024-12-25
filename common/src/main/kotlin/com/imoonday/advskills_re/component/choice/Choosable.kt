package com.imoonday.advskills_re.component.choice

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.nbt.*

abstract class Choosable(val skill: Skill) {

    abstract val type: Type

    open fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("type", this@Choosable.type.ordinal)
        putString("skill", skill.id.toString())
    }

    abstract fun isEmpty(): Boolean

    abstract fun compatibleWith(other: Choosable): Boolean

    enum class Type {
        EMPTY,
        SKILL,
        ENHANCEMENT
    }

    companion object {

        val EMPTY = object : Choosable(Skills.EMPTY) {

            override val type: Type = Type.EMPTY

            override fun isEmpty(): Boolean = true

            override fun compatibleWith(other: Choosable): Boolean = true
        }
    }
}