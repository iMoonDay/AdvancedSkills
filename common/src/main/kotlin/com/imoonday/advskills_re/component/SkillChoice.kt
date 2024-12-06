package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*

data class SkillChoice(
    val first: Skill,
    val second: Skill,
    val third: Skill,
) {

    fun isEmpty() = this === EMPTY || first.invalid && second.invalid && third.invalid

    fun hasEmpty() = this === EMPTY || first.invalid || second.invalid || third.invalid

    fun hasDuplicates() = !isEmpty() && skills.distinct().size < skills.size

    val skills = listOf(first, second, third)

    fun withFirst(skill: Skill): SkillChoice = SkillChoice(skill, second, third)

    fun withSecond(skill: Skill): SkillChoice = SkillChoice(first, skill, third)

    fun withThird(skill: Skill): SkillChoice = SkillChoice(first, second, skill)

    fun replaceWith(filter: (Skill) -> Boolean, generator: (except: MutableSet<Skill>) -> Skill): SkillChoice {
        var choice = this
        val except = skills.toMutableSet()
        if (filter(first)) choice = choice.withFirst(generator(except))
        if (filter(second)) choice = choice.withSecond(generator(except))
        if (filter(third)) choice = choice.withThird(generator(except))
        return choice
    }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putString("1", first.id.toString())
        putString("2", second.id.toString())
        putString("3", third.id.toString())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkillChoice) return false

        if (first != other.first) return false
        if (second != other.second) return false
        if (third != other.third) return false

        return true
    }

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + second.hashCode()
        result = 31 * result + third.hashCode()
        return result
    }

    fun removeDuplicates(): SkillChoice {
        if (!hasDuplicates()) return this
        val distinctSkills = skills.distinct()
        return when (distinctSkills.size) {
            1 -> SkillChoice(distinctSkills[0], Skills.EMPTY, Skills.EMPTY)
            2 -> SkillChoice(distinctSkills[0], distinctSkills[1], Skills.EMPTY)
            else -> this
        }
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