package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.component.choice.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.nbt.*

class SkillChoice(skill: Skill) : Choosable(skill) {

    override val type: Type = Type.SKILL

    override fun isEmpty(): Boolean = skill.disabled

    override fun compatibleWith(other: Choosable): Boolean = other.type != this.type || other.skill != skill

    override fun toString(): String = "SkillChoice(skill=$skill, type=$type)"

    companion object {

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): Choosable =
            Skills.fromIdNullable(nbt.getString("skill"))?.let { SkillChoice(it) } ?: EMPTY
    }
}