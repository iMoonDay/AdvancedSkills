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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Choosable) return false

        if (skill != other.skill) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = skill.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String = "Choosable(skill=$skill, type=$type)"

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

        @JvmStatic
        fun parse(nbt: NbtCompound): Choosable {
            val type = Type.entries.getOrNull(nbt.getInt("type")) ?: return EMPTY
            return when (type) {
                Type.EMPTY -> EMPTY
                Type.SKILL -> SkillChoice.fromNbt(nbt)
                Type.ENHANCEMENT -> EnhancementChoice.fromNbt(nbt)
            }
        }
    }
}