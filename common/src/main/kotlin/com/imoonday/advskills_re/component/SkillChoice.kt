package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*

class SkillChoice(
    first: Skill,
    second: Skill,
    third: Skill,
) : Choice<Skill>(first, second, third) {

    override fun create(first: Skill, second: Skill, third: Skill): Choice<Skill> = SkillChoice(first, second, third)

    override fun isEmpty(item: Skill): Boolean = item.invalid

    override val emptyChoice: Choice<Skill> = EMPTY

    override val emptyItem: Skill = Skills.EMPTY

    override fun toNbt(): NbtCompound = NbtCompound().apply {
        putString("1", first.id.toString())
        putString("2", second.id.toString())
        putString("3", third.id.toString())
    }

    companion object {

        @JvmField
        val EMPTY = SkillChoice(Skills.EMPTY, Skills.EMPTY, Skills.EMPTY)

        fun fromNbt(nbt: NbtCompound): SkillChoice = SkillChoice(
            Skills.fromId(nbt.getString("1")),
            Skills.fromId(nbt.getString("2")),
            Skills.fromId(nbt.getString("3")),
        )

        fun canGenerate(
            except: Collection<Skill> = emptyList(),
            filter: (Skill) -> Boolean = { true }
        ) = Skills.getLearnableSkills(except, filter).isNotEmpty()

        fun generate(
            except: Collection<Skill> = emptyList(),
            filter: (Skill) -> Boolean = { true }
        ): SkillChoice = Skills.getLearnableSkills(except, filter)
            .randomByWeight(Skill::weight, 3, Skills.EMPTY)
            .let { SkillChoice(it[0], it[1], it[2]) }
    }
}